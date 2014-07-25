package org.ietr.preesm.experiment.memory

import java.util.List
import java.util.Map

import static extension org.ietr.preesm.experiment.memory.Range.*

class Match {
	/**
	 * Does not save the match in the buffer matchTable
	 */
	new(Buffer localBuffer, int localIndex, Buffer remoteBuffer, int remoteIndex, int size) {
		this.localBuffer = localBuffer
		this.localIndex = localIndex
		this.remoteBuffer = remoteBuffer
		this.remoteIndex = remoteIndex
		length = size
		this.conflictingMatches = newArrayList
		this.conflictCandidates = newArrayList
		this.applied = false
		this.forbiddenLocalRanges = newArrayList
	}

	@Property
	Buffer localBuffer
	@Property
	int localIndex
	@Property
	Buffer remoteBuffer
	@Property
	int remoteIndex
	@Property
	int length
	@Property
	List<Match> conflictingMatches
	@Property
	List<Match> conflictCandidates

	/**
	 * The {@link MatchType} of the current {@link Match}.
	 */
	@Property
	MatchType type

	/**
	 * Set the {@link #_type type} of the current {@link Match}.
	 * If the type is <code>BACKWARD</code> a new list is created for the
	 * {@link #getMergeableLocalRanges() mergeableLocalRanges}. Otherwise
	 * mergeableLocalRanges is set to <code>null</code>.
	 */
	def setType(MatchType newType) {
		_type = newType
		_mergeableLocalRanges = if (type == MatchType::BACKWARD) {
			newArrayList
		} else {
			null
		}
	}

	/**
	 * This {@link List} contains {@link Range} of the {@link 
	 * #getLocalBuffer()} that can be matched only if:<ul>
	 * <li> The remote buffer has no token for this range (i.e. it is 
	 * out of the minIndex .. maxIndex range).</li></ul>
	 * The list of a {@link Match} and the one of its {@link #getReciprocate()
	 * reciprocate} are not equals.
	 */
	@Property
	List<Range> forbiddenLocalRanges

	/**
	 * This {@link List} contains {@link Range} of the {@link 
	 * #getLocalBuffer()} that can be matched only if:<ul>
	 * <li> If the match is backward AND both the remote buffers are 
	 * mergeable for this ranges.<br>
	 * or</li>
	 * <li> The remote buffer has no token for this range (i.e. it is 
	 * out of the minIndex .. maxIndex range).</li></ul>
	 * Only {@link #getType() backward} matches can have mergeableLocalRanges.
	 */
	@Property
	List<Range> mergeableLocalRanges

	/**
	 * This {@link boolean} is set to <code>true</code> if the current {@link 
	 * Match} was applied.
	 */
	@Property
	boolean applied

	Match _reciprocate

	def setReciprocate(Match remoteMatch) {
		_reciprocate = remoteMatch
		remoteMatch._reciprocate = this
	}

	def getReciprocate() {
		_reciprocate
	}

	/**
	 * Returns a {@link Range} going from {@link Match#getLocalIndex() 
	 * localIndex} to the end of the matched tokens.
	 * 
	 */
	def getLocalRange() {
		new Range(localIndex, localIndex + length)
	}

	/**
	 * Get the indivisible {@link Range} in which the current {@link Match}
	 * falls. This method has no side-effects.
	 * 
	 * @return the {@link Range} resulting from the {@link 
	 * Range#lazyUnion(List,Range) lazyUnion} of the {@link 
	 * Match#getLocalRange() localRange} and the {@link 
	 * Buffer#getIndivisibleRanges() indivisibleRanges} of the {@link 
	 * Match#getLocalBuffer() localBuffer}.
	 * 
	 */
	def Range getLocalIndivisibleRange() {

		val localIndivisiblerange = this.localRange

		// Copy the overlapping indivisible range(s)
		val overlappingIndivisibleRanges = this.localBuffer.indivisibleRanges.filter [
			it.hasOverlap(localIndivisiblerange)
		].map[it.clone as Range].toList // toList to make sure the map function is applied only once

		// Do the lazy union of the match and its overlapping indivisible 
		// ranges
		overlappingIndivisibleRanges.lazyUnion(localIndivisiblerange)
	}

	/**
	 * Returns the {@link Range} of the {@link Match#getLocalBuffer() 
	 * localBuffer} that will be impacted if <code>this</code> {@link Match} 
	 * is applied. This {@link Range} corresponds to the largest {@link Range}
	 * between the {@link #getLocalRange()} of the current {@link Match} and 
	 * the {@link Match#getLocalIndivisibleRange() localIndivisibleRange} of 
	 * the {@link #getReciprocate() reciprocate} {@link Match}. 
	 * 
	 * @return a {@link Range} of impacted tokens aligned with the {@link 
	 * Match#getLocalBuffer() localBuffer} indexes.
	 */
	def Range getLocalImpactedRange() {

		// Get the aligned smallest indivisible range (local or remote)
		val localRange = this.localRange
		val remoteIndivisibleRange = this.reciprocate.localIndivisibleRange
		remoteIndivisibleRange.translate(this.localIndex - this.remoteIndex)
		val smallestRange = if (localRange.length > remoteIndivisibleRange.length) {
				localRange
			} else {
				remoteIndivisibleRange
			}

		smallestRange
	}

	/**
	 * Overriden to forbid
	 */
	override hashCode() {

		// Forbidden because if a non final attribute is changed, then the hashcode changes.
		// But if the Match is already stored in a map, its original hashcode will have been used
		throw new UnsupportedOperationException("HashCode is not supported for Match class. Do not use HashMap.")

	//index.hashCode.bitwiseXor(length.hashCode).bitwiseXor(buffer.hashCode) 	
	}

	/**
	 * Reciprocate is not considered 
 	*/
	override equals(Object obj) {
		if (this === obj)
			return true
		if (obj === null)
			return false
		if (this.class != obj.class)
			return false
		var other = obj as Match
		this.localBuffer == other.localBuffer && this.localIndex == other.localIndex &&
			this.remoteBuffer == other.remoteBuffer && this.remoteIndex == other.remoteIndex &&
			this.length == other.length
	}

	override toString() '''«localBuffer.dagVertex.name».«localBuffer.name»[«localIndex»..«localIndex + length»[=>«remoteBuffer.
		dagVertex.name».«remoteBuffer.name»[«remoteIndex»..«remoteIndex + length»['''

	/**
	 * Check whether the current match fulfills its forbiddenLocalRanges and
	 * mergeableLocalRange conditions. Does not check the reciprocate.
	 */
	def isApplicable() {

		// Does not match forbidden tokens
		val impactedTokens = this.localImpactedRange.intersection(
			new Range(this.localBuffer.minIndex, this.localBuffer.maxIndex))
		this.forbiddenLocalRanges.intersection(impactedTokens).size == 0 &&
		// And match only localMergeableRange are in fact mergeable 
		if (this.type == MatchType::FORWARD) {
			true
		} else {
			val mustBeMergeableRanges = this.mergeableLocalRanges.intersection(impactedTokens)
			val mergeableRanges = this.localBuffer.mergeableRanges.intersection(impactedTokens)
			mustBeMergeableRanges.difference(mergeableRanges).size == 0
		}
	}

	/** 
	 * Recursive method to find where the {@link #getLocalRange()} of the 
	 * current {@link #getLocalBuffer() localBuffer} is matched.
	 * 
	 * @return a {@link Map} that associates: a {@link Range subranges} of the 
	 * {@link #getLocalRange() localRange} of the  {@link #getLocalBuffer() 
	 * localBuffer} to a {@link Pair} composed of a {@link Buffer} and a 
	 * {@link Range} where the associated {@link Range subrange} is matched.
	 * 
	 */
	def Map<Range, Pair<Buffer, Range>> getRoot() {
		val result = newHashMap
		val remoteRange = this.localIndivisibleRange.translate(this.remoteIndex - this.localIndex)

		// Termination case if the remote Buffer is not matched	
		if (this.remoteBuffer.matched == null) {
			result.put(
				this.localIndivisibleRange,
				this.remoteBuffer -> remoteRange
			)
		}
		// Else, recursive call 
		else {
			for (match : this.remoteBuffer.matched.filter[it.localIndivisibleRange.hasOverlap(remoteRange)]) {
				val recursiveResult = match.root
				for (entry : recursiveResult.entrySet) {
					val range = entry.key

					// translate back to local range
					range.translate(this.localIndex - this.remoteIndex)
					result.put(range, entry.value)
				}
			}
		}

		return result
	}

}

/**
 * This enumeration represent the type of the current {@link Match}
 */
public enum MatchType {

	/**
	 * The {@link Match} links several inputs (or outputs) together.
	 * Not allowed anymore
	 */
	//INTER_SIBLINGS,
	/**
	 * The {@link Match} is internal to an actor and links an input {@link 
	 * Buffer} to an output {@link Buffer}, <b>or</b> the {@link Match} is
	 * external to an actor (i.e. correspond to an edge) and it links an output
	 * {@link Buffer} of an actor to the input {@link Buffer} of the next.
	 */
	FORWARD,

	/**
	 * Opposite of {@link MatchType.FORWARD}
	 */
	BACKWARD
}
