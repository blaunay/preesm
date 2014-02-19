package org.ietr.preesm.experiment.memory

import java.util.ArrayList
import java.util.List
import java.util.Map
import org.ietr.dftools.algorithm.model.dag.DAGVertex
import org.ietr.dftools.algorithm.model.sdf.SDFAbstractVertex
import org.ietr.dftools.algorithm.model.sdf.SDFEdge

import static extension org.ietr.preesm.experiment.memory.Range.*

class Buffer {

	/**
	 * Identify which data ranges of a {@link Buffer} are matched multiple
	 * times. A range is matched multiple times if several matches involving
	 * this ranges are stored in the {@link Buffer#getMatchTable() match table}
	 * of the {@link Buffer}. For example, if these to calls are executed: </br>
	 * <code>a.matchWith(0,b,0,3)</code> and <code>a.matchWith(0,b,3,3)</code>,
	 * then a[0..3[ is matched multiple times.   
	 * 
	 * 
	 * @return a {@link Map} containing the start end end of ranges matched
	 *         multiple times.
	 */
	static def getMultipleMatchRange(Buffer buffer) {

		val matchRanges = newArrayList
		val multipleMatchRanges = newArrayList

		// For each matchList
		buffer.matchTable.forEach [ localIdx, matchList |
			// For each Match
			matchList.forEach [ match |
				val newRange = new Range(localIdx, localIdx + match.length)
				// Get the intersection of the match and existing match ranges
				val intersections = matchRanges.intersection(newRange)
				multipleMatchRanges.union(intersections)
				// Update the existing match ranges
				matchRanges.union(newRange)
			]
		]

		multipleMatchRanges
	}

	/**
	 * Test if the {@link Buffer} is partially matched.<br>
	 * <br>
	 * A {@link Buffer} is partially matched if only part of its token range 
	 * (i.e. from {@link Buffer#_minIndex minIndex} to {@link Buffer#_maxIndex
	 *  maxIndex}) are involved in a {@link Match} in the {@link Buffer} 
	 * {@link Buffer#_matchTable match table}.
	 * 
	 * @param buffer
	 * 	The tested {@link Buffer}.
	 * @return <code>true</code> if the {@link Buffer} is completely matched,
	 * and <code>false</code> otherwise. 
	 */
	static def isCompletelyMatched(Buffer buffer) {
		val coveredRange = new ArrayList<Range>
		val iterEntry = buffer.matchTable.entrySet.iterator
		var stop = false

		while (iterEntry.hasNext && !stop) {
			val entry = iterEntry.next
			val localIdx = entry.key
			var iterMatch = entry.value.iterator
			while (iterMatch.hasNext && !stop) {
				val match = iterMatch.next
				val addedRange = coveredRange.union(new Range(localIdx, localIdx + match.length))

				// Set stop to true if the range covers the complete token range
				stop = stop || (addedRange.start == buffer.minIndex && addedRange.end == buffer.maxIndex)
			}
		}

		// If the loops were stopped, a complete range was reached
		stop
	}

	/**
	 * Test if all {@link Match matches} contained in the {@link 
	 * Buffer#_machTable matchTable} are reciprocal.<br><br>
	 * 
	 * A {@link Match} is reciprocal if the remote {@link Match#buffer}
     * contains an reciprocal {@link Match} in its {@link Buffer#_matchTable
     * matchTable}.  
	 */
	static def isReciprocal(Buffer localBuffer) {
		localBuffer.matchTable.entrySet.forall [
			val matches = it.value
			val localIdx = it.key
			// for all matches
			matches.forall [ match |
				val remoteMatches = match.remoteBuffer.matchTable.get(match.remoteIndex)
				remoteMatches != null && remoteMatches.contains(
					new Match(match.remoteBuffer, match.remoteIndex, localBuffer, localIdx, match.length))
			]
		]
	}

	/**
	 * The objective of this method is to merge as many matches as possible
	 * from the {@link Buffer} {@link Buffer#_matchTable match tables}.<br><br>
	 * 
	 * Two matches are mergeable if they are consecutive and if they match 
	 * consecutive targets.<br>
	 * Example 1: <code>a[0..3]<->b[1..4] and a[4..5]<->b[5..6]</code> are
	 * valid candidates.<br>
	 * Example 2: <code>a[0..3]<->b[1..4] and a[5..6]<->b[5..6]</code> are
	 * not valid candidates.<br><b>
	 * Before using this method, the {@link Buffer} must pass all checks
	 * performed by the {@link ScriptRunner#check()} method. </b>
	 * 
	 * @param buffer
	 * 	The {@link Buffer} whose {@link Buffer#_matchTable matchTable} is
	 *  simplified.
	 * @param processedMatch
	 * 	A {@link List} containing {@link Match matches} that will be ignored
	 *  during simplification. This list will be updated during the method
	 *  execution by adding to it the {@link Match#reciprocate} of the 
	 *  processed {@link Match matches}. 
	 */
	static def simplifyMatches(Buffer buffer, List<Match> processedMatch) {
		val removedEntry = newArrayList

		// Process the match table
		buffer.matchTable.entrySet.forEach [
			val localIdx = it.key
			val matchSet = it.value
			// For each match
			matchSet.filter[!processedMatch.contains(it)].forEach [ match |
				var Match remMatch = null
				do {

					// Check if a consecutive match exist
					var candidateSet = buffer.matchTable.get(localIdx + match.length)

					// Since Buffer#check() is supposed valid
					// at most one candidate can satisfy the conditions
					remMatch = if (candidateSet != null) {
						candidateSet.findFirst [ candidate |
							candidate.remoteBuffer == match.remoteBuffer // same target
							&& candidate.remoteIndex == match.remoteIndex + match.length
						]
					} else {
						null
					}
					if (remMatch != null) {

						// Remove the consecutive match from matchTables
						candidateSet.remove(remMatch)
						val remMatchSet = remMatch.remoteBuffer.matchTable.get(remMatch.remoteIndex)
						remMatchSet.remove(remMatch.reciprocate)

						// Remove empty matchLists from the matchTable
						if(remMatchSet.size == 0) remMatch.remoteBuffer.matchTable.remove(remMatch.remoteIndex)
						if(candidateSet.size == 0) removedEntry.add(localIdx + match.length)

						// Lengthen the existing match
						match.length = match.length + remMatch.length
						match.reciprocate.length = match.length
					}
				} while (remMatch != null)
				// Put the reciprocate match in the in the processes list
				processedMatch.add(match.reciprocate)
			]
		]

		// Remove empty matchLists from matchTable
		removedEntry.forEach[buffer.matchTable.remove(it)]
	}

	/**
	 * Minimum index for the buffer content.
	 * Constructor initialize this value to 0 but it is possible to lower this
	 * value by matching another buffer on the "edge" of this one.<br>
	 * For example: <code>this.matchWith(-3, a, 0, 6)</code> results in 
	 * matching this[-3..2] with a[0..5], thus lowering this.minIndex to -3.
	 */
	int _minIndex

	/**
	 * cf {@link #minIndex}.
	 */
	int _maxIndex

	/**
	 * This table is protected to ensure that matches are set only by using
	 * {@link #matchWith(int,Buffer,int)} methods in the scripts.
	 */
	@Property
	final protected Map<Integer, List<Match>> matchTable

	@Property
	final String name

	@Property
	final int nbTokens

	@Property
	final int tokenSize

	@Property
	final DAGVertex dagVertex

	@Property
	final SDFEdge sdfEdge

	@Property
	boolean indivisible

	@Property
	final protected Map<Range, Pair<Buffer, Integer>> appliedMatches
	
	@Property
	List<Range> mergeableRanges

	/**
    * Constructor for the {@link Buffer}.
    * @param name
    * 	A {@link String} corresponding to the final name of the buffer. 
    * @param nbTokens
    * 	The number of tokens stored in this buffer.
    * @param tokenSize
    * 	The size of one token of the buffer.
    */
	new(SDFEdge edge, DAGVertex dagVertex, String name, int nbTokens, int tokenSize, boolean mergeable) {
		_sdfEdge = edge
		_name = name
		_nbTokens = nbTokens
		_tokenSize = tokenSize
		_matchTable = newHashMap()
		_appliedMatches = newHashMap()
		_minIndex = 0
		_maxIndex = nbTokens * tokenSize
		_indivisible = true
		_dagVertex = dagVertex
		_mergeableRanges = newArrayList
		if(mergeable){
			_mergeableRanges.add(new Range(0, nbTokens*tokenSize))
		}
	}

	def getSdfVertex() {
		dagVertex.getPropertyBean().getValue(DAGVertex.SDF_VERTEX, SDFAbstractVertex) as SDFAbstractVertex
	}

	def getMinIndex() {
		_minIndex
	}

	def getMaxIndex() {
		_maxIndex
	}

	/**
	* Cf. {@link Buffer#matchWith(int, Buffer, int, int)} with size = 1
 	*/
	def matchWith(int localIdx, Buffer buffer, int remoteIdx) {
		matchWith(localIdx, buffer, remoteIdx, 1)
	}

	/**
	 * {@link Match} part of the current {@link Buffer} with part of another
	 * {@link Buffer}. Example: <code>a.matchWith(3,b,7,5)</code> matches
	 * a[3..7] with b[7..11]. Matching two {@link Buffer buffers} means that the
	 * matched ranges may be merged, i.e. they may be allocated in the same
	 * memory space.<br>
	 * The localIdx, remoteIdx and size represent a number of token. (cf.
	 * production and consumption rate from the SDF graph).
	 * 
	 * @exception Exception
	 *                may be thrown if the matched ranges both have elements
	 * 				  outside of their {@link Buffer} indexes 
	 * 				  ({@link #_maxIndex} and {@link #_minIndex}). 
	 *                
	 * 
	 * @param localIdx
	 *            start index of the matched range for the local {@link Buffer}.
	 * @param buffer
	 *            remote {@link Buffer}
	 * @param remoteIdx
	 *            start index of the matched range for the remote {@link Buffer}
	 * @param size
	 *            the size of the matched range
	 * @return the created local {@link Match}
	 */
	def matchWith(int localIdx, Buffer buffer, int remoteIdx, int size) {

		if (this.tokenSize != buffer.tokenSize) {
			throw new RuntimeException(
				'''Cannot match «this.dagVertex.name».«this.name» with «buffer.dagVertex.name».«buffer.name» because buffers have different token sized («this.
					tokenSize» != «buffer.tokenSize» ")''')
		}

		// Test if a matched range is completely out of real tokens
		if ((localIdx >= this.nbTokens) || (localIdx + size - 1 < 0)) {
			throw new RuntimeException(
				'''Cannot match «this.dagVertex.name».«this.name»[«localIdx»..«(localIdx + size - 1)»] and «buffer.
					dagVertex.name».«buffer.name»[«remoteIdx»..«(remoteIdx + size - 1)»] because no "real" token from «this.
					dagVertex.name».«this.name»[0..«(this.nbTokens - 1)»] is matched.''')
		}
		if ((remoteIdx >= buffer.nbTokens) || (remoteIdx + size - 1 < 0)) {
			throw new RuntimeException(
				'''Cannot match «this.dagVertex.name».«this.name»[«localIdx»..«localIdx + size - 1»] and «buffer.
					dagVertex.name».«buffer.name»[«remoteIdx»..«remoteIdx + size - 1»] because no "real" token from "«buffer.
					dagVertex.name».«buffer.name»[0..«buffer.nbTokens - 1»] is matched.''')
		}

		// Are "virtual" tokens matched together
		if (// Both ranges begins before the first token
		((localIdx < 0) && (remoteIdx < 0)) ||
			// or both buffers ends after the last token
			((localIdx + size - 1 >= this.nbTokens) && (remoteIdx + size - 1 >= buffer.nbTokens)) ||
			// or local range begins with less real tokens than the number of virtual tokens beginning remote range
			(localIdx >= 0 && ((this.nbTokens - localIdx) <= -Math::min(0, remoteIdx))) ||
			// or remote range begins with less real tokens than the number of virtual tokens beginning local range
			(remoteIdx >= 0 && ((buffer.nbTokens - remoteIdx) <= -Math::min(0, localIdx)))) {
			throw new RuntimeException(
				'''Cannot match «this.dagVertex.name».«this.name»[«localIdx»..«localIdx + size - 1»] and «buffer.
					dagVertex.name».«buffer.name»[«remoteIdx»..«(remoteIdx + size - 1)»] because "virtual tokens" cannot be matched together.«"\n"»Information: «this.
					dagVertex.name».«this.name» size = «this.nbTokens» and «buffer.dagVertex.name».«buffer.name» size = «buffer.
					nbTokens».''')
		}

		this.byteMatchWith(localIdx * tokenSize, buffer, remoteIdx * tokenSize, size * tokenSize)

	/*
		// If needed, update the buffers min/max indexes
		if (!(localIdx >= 0) && (localIdx + size - 1 < this.nbTokens)) {
			this._minIndex = Math::min(_minIndex, localIdx * tokenSize)
			this._maxIndex = Math::max(_maxIndex, (localIdx + size) * tokenSize)
		}
		if (!(remoteIdx >= 0) && (remoteIdx + size - 1 < buffer.nbTokens)) {
			buffer._minIndex = Math::min(buffer._minIndex, remoteIdx * tokenSize)
			buffer._maxIndex = Math::max(buffer._maxIndex, (remoteIdx + size) * tokenSize)
		}

		// Do the match
		var matchSet = matchTable.get(localIdx * tokenSize)
		if (matchSet == null) {
			matchSet = newArrayList
			matchTable.put(localIdx * tokenSize, matchSet)
		}
		val localMatch = new Match(this, localIdx * tokenSize, buffer, remoteIdx * tokenSize, size * this.tokenSize)
		matchSet.add(localMatch)

		var remoteMatchSet = buffer.matchTable.get(remoteIdx * tokenSize)
		if (remoteMatchSet == null) {
			remoteMatchSet = newArrayList
			buffer.matchTable.put(remoteIdx * tokenSize, remoteMatchSet)
		}
		val remoteMatch = new Match(buffer, remoteIdx * tokenSize, this, localIdx * tokenSize, size * this.tokenSize)
		remoteMatchSet.add(remoteMatch)

		localMatch.reciprocate = remoteMatch
		return localMatch*/
	}

	def byteMatchWith(int localByteIdx, Buffer buffer, int remoteByteIdx, int byteSize) {
		byteMatchWith(localByteIdx, buffer, remoteByteIdx, byteSize, true)
	}

	def byteMatchWith(int localByteIdx, Buffer buffer, int remoteByteIdx, int byteSize, boolean check) {

		// Test if a matched range is completely out of real bytes
		if (check) {
			if ((localByteIdx >= this.nbTokens * this.tokenSize) || (localByteIdx + byteSize - 1 < 0)) {
				throw new RuntimeException(
					'''Cannot match bytes «this.dagVertex.name».«this.name»[«localByteIdx»..«(localByteIdx + byteSize -
						1)»] and «buffer.dagVertex.name».«buffer.name»[«remoteByteIdx»..«(remoteByteIdx + byteSize - 1)»] because no "real" byte from «this.
						dagVertex.name».«this.name»[0..«(this.nbTokens * this.tokenSize - 1)»] is matched.''')
			}
			if ((remoteByteIdx >= buffer.nbTokens * buffer.tokenSize) || (remoteByteIdx + byteSize - 1 < 0)) {
				throw new RuntimeException(
					'''Cannot match bytes «this.dagVertex.name».«this.name»[«localByteIdx»..«localByteIdx + byteSize - 1»] and «buffer.
						dagVertex.name».«buffer.name»[«remoteByteIdx»..«remoteByteIdx + byteSize - 1»] because no "real" byte from "«buffer.
						dagVertex.name».«buffer.name»[0..«buffer.nbTokens * buffer.tokenSize - 1»] is matched.''')
			}

			// Are "virtual" tokens matched together
			if (// Both ranges begins before the first byte
			((localByteIdx < 0) && (remoteByteIdx < 0)) ||
				// or both buffers ends after the last byte
			((localByteIdx + byteSize - 1 >= this.nbTokens * this.tokenSize) &&
				(remoteByteIdx + byteSize - 1 >= buffer.nbTokens * buffer.tokenSize)) ||
				// or local range begins with less real bytes than the number of virtual bytes beginning remote range
				(localByteIdx >= 0 && ((this.nbTokens * this.tokenSize - localByteIdx) <= -Math::min(0, remoteByteIdx))) ||
				// or remote range begins with less real bytes than the number of virtual bytes beginning local range
				(remoteByteIdx >= 0 &&
					((buffer.nbTokens * buffer.tokenSize - remoteByteIdx) <= -Math::min(0, localByteIdx)))) {
				throw new RuntimeException(
					'''Cannot match bytes «this.dagVertex.name».«this.name»[«localByteIdx»..«localByteIdx + byteSize - 1»] and «buffer.
						dagVertex.name».«buffer.name»[«remoteByteIdx»..«(remoteByteIdx + byteSize - 1)»] because "virtual bytes" cannot be matched together.«"\n"»Information: «this.
						dagVertex.name».«this.name» size = «this.nbTokens * this.tokenSize» and «buffer.dagVertex.name».«buffer.
						name» size = «buffer.nbTokens * buffer.tokenSize».''')
			}
		}

		// If needed, update the buffers min/max indexes
		if (!(localByteIdx >= 0 && localByteIdx + byteSize - 1 < this.nbTokens * this.tokenSize)) {
			this._minIndex = Math::min(_minIndex, localByteIdx)
			this._maxIndex = Math::max(_maxIndex, (localByteIdx + byteSize))
		}
		if (!(remoteByteIdx >= 0 && remoteByteIdx + byteSize - 1 < buffer.nbTokens * buffer.tokenSize)) {
			buffer._minIndex = Math::min(buffer._minIndex, remoteByteIdx)
			buffer._maxIndex = Math::max(buffer._maxIndex, (remoteByteIdx + byteSize))
		}

		// Do the match
		var matchSet = matchTable.get(localByteIdx)
		if (matchSet == null) {
			matchSet = newArrayList
			matchTable.put(localByteIdx, matchSet)
		}
		val localMatch = new Match(this, localByteIdx, buffer, remoteByteIdx, byteSize)
		matchSet.add(localMatch)

		var remoteMatchSet = buffer.matchTable.get(remoteByteIdx)
		if (remoteMatchSet == null) {
			remoteMatchSet = newArrayList
			buffer.matchTable.put(remoteByteIdx, remoteMatchSet)
		}
		val remoteMatch = new Match(buffer, remoteByteIdx, this, localByteIdx, byteSize)
		remoteMatchSet.add(remoteMatch)

		localMatch.reciprocate = remoteMatch
		return localMatch
	}

	override toString() '''«sdfVertex.name».«name»[«nbTokens * tokenSize»]'''

	/**
	 * We do not check that the match is possible !
	 * The local buffer is merged into the remote buffer
	 * The local buffer does not "exists" afterwards
	 */
	def applyMatch(Match match) {

		// Temp version with unique match for a complete buffer
		appliedMatches.put(new Range(match.localIndex, match.localIndex + match.length),
			match.remoteBuffer -> match.remoteIndex)

		// Move all third-party matches from the merged buffer
		match.localBuffer.matchTable.values.flatten.filter[it != match].toList.immutableCopy.forEach [ movedMatch |
			// Remove old match from original match list
			val localList = match.localBuffer.matchTable.get(movedMatch.localIndex)
			localList.remove(movedMatch)
			if (localList.size == 0) {
				match.localBuffer.matchTable.remove(movedMatch.localIndex)
			}
			// Change the match local buffer and index	
			// Length and remoteBuffer are unchanged
			movedMatch.localBuffer = match.remoteBuffer
			movedMatch.localIndex = movedMatch.localIndex - (match.localIndex - match.remoteIndex)
			// Update the reciprocate
			movedMatch.reciprocate.remoteBuffer = movedMatch.localBuffer
			movedMatch.reciprocate.remoteIndex = movedMatch.localIndex
			// Put the moved match in its new host matchTable
			var matchList = match.remoteBuffer.matchTable.get(movedMatch.localIndex)
			if (matchList === null) {
				matchList = newArrayList
				match.remoteBuffer.matchTable.put(movedMatch.localIndex, matchList)
			}
			matchList.add(movedMatch)
		]

		// Remove the applied match from the buffers match table 
		// (local and reciprocate)
		match.unmatch

		// Match was applied (and reciprocate)
		match.applied = true
		match.reciprocate.applied = true

		// TODO: update the division points if any
		if (ScriptRunner::printTodo) {
			println("Update division points in match application")
		}
	}

	def unmatch(Match match) {
		val it = match

		// Local unmatch
		val localList = localBuffer.matchTable.get(localIndex)
		localList.remove(it)
		if (localList.size == 0) {
			localBuffer.matchTable.remove(localIndex)
		}

		// Remote unmatch
		val remoteList = remoteBuffer.matchTable.get(remoteIndex)
		remoteList.remove(reciprocate)
		if (remoteList.size == 0) {
			remoteBuffer.matchTable.remove(remoteIndex)
		}

	}
}
