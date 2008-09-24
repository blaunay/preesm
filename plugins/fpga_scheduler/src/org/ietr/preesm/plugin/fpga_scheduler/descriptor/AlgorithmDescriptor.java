package org.ietr.preesm.plugin.fpga_scheduler.descriptor;

import java.util.HashMap;
import java.util.Vector;

import org.sdf4j.factories.DAGEdgeFactory;
import org.sdf4j.model.dag.DirectedAcyclicGraph;

public class AlgorithmDescriptor extends DirectedAcyclicGraph {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2359903713845636462L;

	private String name = "algorithm";

	private HashMap<String, ComputationDescriptor> ComputationDescriptorBuffer;

	private HashMap<String, CommunicationDescriptor> CommunicationDescriptorBuffer;

	private HashMap<String, OperationDescriptor> OperationDescriptorBuffer;

	private ComputationDescriptor topComputation;

	private ComputationDescriptor bottomComputation;

	public AlgorithmDescriptor(DAGEdgeFactory arg0) {
		super(arg0);
		ComputationDescriptorBuffer = new HashMap<String, ComputationDescriptor>();
		CommunicationDescriptorBuffer = new HashMap<String, CommunicationDescriptor>();
		OperationDescriptorBuffer = new HashMap<String, OperationDescriptor>();
		topComputation = new ComputationDescriptor("topComputation",
				ComputationDescriptorBuffer);
		topComputation.setAlgorithm(this);
		this.addComputation(topComputation);
		bottomComputation = new ComputationDescriptor("bottomComputation",
				ComputationDescriptorBuffer);
		bottomComputation.setAlgorithm(this);
		this.addComputation(bottomComputation);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ComputationDescriptor getTopComputation() {
		return topComputation;
	}

	public ComputationDescriptor getBottomComputation() {
		return bottomComputation;
	}

	// Computation
	public void addComputation(ComputationDescriptor computation) {
		if (!ComputationDescriptorBuffer.containsValue(computation)) {
			ComputationDescriptorBuffer.put(computation.getVertex().getName(),
					computation);
			OperationDescriptorBuffer.put(computation.getVertex().getName(),
					computation);
			this.addVertex(computation.getVertex());
		}
	}

	public ComputationDescriptor getComputation(String name) {
		return ComputationDescriptorBuffer.get(name);
	}

	public HashMap<String, ComputationDescriptor> getComputations() {
		return ComputationDescriptorBuffer;
	}

	// Communication
	public void addCommunication(CommunicationDescriptor communication) {
		if (!CommunicationDescriptorBuffer.containsValue(communication)) {
			CommunicationDescriptorBuffer.put(
					communication.getEdge().getName(), communication);
			OperationDescriptorBuffer.put(communication.getEdge().getName(),
					communication);
			if (communication.getName().equalsIgnoreCase("topCommunication")
					&& communication.getName().equalsIgnoreCase(
							"bottomCommunication")) {
				this.addEdge(ComputationDescriptorBuffer.get(
						communication.getSource()).getVertex(),
						ComputationDescriptorBuffer.get(
								communication.getDestination()).getVertex());
			}
		}
	}

	public CommunicationDescriptor getCommunication(String name) {
		return CommunicationDescriptorBuffer.get(name);
	}

	public CommunicationDescriptor getCommunication(ComputationDescriptor src,
			ComputationDescriptor dst) {
		return CommunicationDescriptorBuffer.get(this.getEdge(src.getVertex(),
				dst.getVertex()).getName());
	}

	public HashMap<String, CommunicationDescriptor> getCommunications() {
		return CommunicationDescriptorBuffer;
	}

	// Operation
	public OperationDescriptor getOperation(String name) {
		return OperationDescriptorBuffer.get(name);
	}

	public HashMap<String, OperationDescriptor> getOperations() {
		return OperationDescriptorBuffer;
	}

	/**
	 * 
	 */
	public void computeTopLevel() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		topComputation.setTopLevel(0);
		topComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they may be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getDestination()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getPrecedingCommunications()) {
				if (!this.getComputation(indexCommunication.getSource())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					if (time < this.getComputation(
							indexCommunication.getSource()).getTopLevel()
							+ this.getComputation(
									indexCommunication.getSource())
									.getComputationDuration()
							+ indexCommunication.getCommunicationDuration()) {
						time = this.getComputation(
								indexCommunication.getSource()).getTopLevel()
								+ this.getComputation(
										indexCommunication.getSource())
										.getComputationDuration()
								+ indexCommunication.getCommunicationDuration();
					}
				}
			}
			if (!skipComputation) {
				computationList.get(i).setTopLevel(time);
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getName() + " --> t-level="
				// + computationList.get(i).getTopLevel());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getFollowingCommunications()) {
					if (this
							.getComputation(indexCommunication.getDestination()) != bottomComputation) {
						computationList.add(this
								.getComputation(indexCommunication
										.getDestination()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			if (time < this.getComputation(indexCommunication.getSource())
					.getTopLevel()
					+ this.getComputation(indexCommunication.getSource())
							.getComputationDuration()) {
				time = this.getComputation(indexCommunication.getSource())
						.getTopLevel()
						+ this.getComputation(indexCommunication.getSource())
								.getComputationDuration();
			}
		}
		bottomComputation.setTopLevel(time);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.setASAP(indexComputation.getTopLevel());
		}
	}

	/**
	 * 
	 */
	public void computeBottomLevel() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		bottomComputation.setBottomLevel(0);
		bottomComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they should be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getSource()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getFollowingCommunications()) {
				if (!this.getComputation(indexCommunication.getDestination())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					if (time < this.getComputation(
							indexCommunication.getDestination())
							.getBottomLevel()
							+ indexCommunication.getCommunicationDuration()) {
						time = this.getComputation(
								indexCommunication.getDestination())
								.getBottomLevel()
								+ indexCommunication.getCommunicationDuration();
					}
				}
			}
			if (!skipComputation) {
				computationList.get(i).setBottomLevel(
						time + computationList.get(i).getComputationDuration());
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getId() + " --> b-level="
				// + computationList.get(i).getBottomLevel());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					if (this.getComputation(indexCommunication.getSource()) != topComputation) {
						computationList
								.add(this.getComputation(indexCommunication
										.getSource()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			if (time < this.getComputation(indexCommunication.getDestination())
					.getBottomLevel()) {
				time = this.getComputation(indexCommunication.getDestination())
						.getBottomLevel();
			}
		}
		topComputation.setBottomLevel(time);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.setALAP(topComputation.getBottomLevel()
					- indexComputation.getBottomLevel());
		}
	}

	public Vector<ComputationDescriptor> sortComputationsByBottomLevel() {
		Vector<ComputationDescriptor> sortedComputations = new Vector<ComputationDescriptor>();
		computeTopLevel();
		computeBottomLevel();
		sortedComputations.add(topComputation);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				for (int i = 0; i < sortedComputations.size(); i++) {
					if (indexComputation.getBottomLevel() > sortedComputations
							.get(i).getBottomLevel()) {
						sortedComputations.add(i, indexComputation);
						break;
					} else {
						if (indexComputation.getBottomLevel() == sortedComputations
								.get(i).getBottomLevel()) {
							if (indexComputation.getTopLevel() > sortedComputations
									.get(i).getTopLevel()) {
								sortedComputations.add(i, indexComputation);
								break;
							}
						}
						if (i == (sortedComputations.size() - 1)) {
							sortedComputations.add(indexComputation);
							break;
						}
					}
				}
			}
		}
		sortedComputations.remove(0);
		return sortedComputations;
	}

	/**
	 * 
	 */
	public void computeTopLevelComputation() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		topComputation.setTopLevelComputation(0);
		topComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they may be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getDestination()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getPrecedingCommunications()) {
				if (!this.getComputation(indexCommunication.getSource())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					if (time < this.getComputation(
							indexCommunication.getSource())
							.getTopLevelComputation()
							+ this.getComputation(
									indexCommunication.getSource())
									.getComputationDuration()) {
						time = this.getComputation(
								indexCommunication.getSource())
								.getTopLevelComputation()
								+ this.getComputation(
										indexCommunication.getSource())
										.getComputationDuration();
					}
				}
			}
			if (!skipComputation) {
				computationList.get(i).setTopLevelComputation(time);
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getName() + " --> t-level-c="
				// + computationList.get(i).getTopLevelComputation());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getFollowingCommunications()) {
					if (this
							.getComputation(indexCommunication.getDestination()) != bottomComputation) {
						computationList.add(this
								.getComputation(indexCommunication
										.getDestination()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			if (time < this.getComputation(indexCommunication.getSource())
					.getTopLevel()
					+ this.getComputation(indexCommunication.getSource())
							.getComputationDuration()) {
				time = this.getComputation(indexCommunication.getSource())
						.getTopLevel()
						+ this.getComputation(indexCommunication.getSource())
								.getComputationDuration();
			}
		}
		bottomComputation.setTopLevelComputation(time);
	}

	/**
	 * 
	 */
	public void computeBottomLevelComputation() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		bottomComputation.setBottomLevelComputation(0);
		bottomComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they should be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getSource()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getFollowingCommunications()) {
				if (!this.getComputation(indexCommunication.getDestination())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					if (time < this.getComputation(
							indexCommunication.getDestination())
							.getBottomLevelComputation()) {
						time = this.getComputation(
								indexCommunication.getDestination())
								.getBottomLevelComputation();
					}
				}
			}
			if (!skipComputation) {
				computationList.get(i).setBottomLevelComputation(
						time + computationList.get(i).getComputationDuration());
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getId() + " --> b-level-c="
				// + computationList.get(i).getBottomLevelComputation());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					if (this.getComputation(indexCommunication.getSource()) != topComputation) {
						computationList
								.add(this.getComputation(indexCommunication
										.getSource()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			if (time < this.getComputation(indexCommunication.getDestination())
					.getBottomLevelComputation()) {
				time = this.getComputation(indexCommunication.getDestination())
						.getBottomLevelComputation();
			}
		}
		topComputation.setBottomLevelComputation(time);
	}

	public Vector<ComputationDescriptor> sortComputationsByBottomLevelComputation() {
		Vector<ComputationDescriptor> sortedComputations = new Vector<ComputationDescriptor>();
		computeTopLevelComputation();
		computeBottomLevelComputation();
		sortedComputations.add(topComputation);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				for (int i = 0; i < sortedComputations.size(); i++) {
					if (indexComputation.getBottomLevelComputation() > sortedComputations
							.get(i).getBottomLevelComputation()) {
						sortedComputations.add(i, indexComputation);
						break;
					} else {
						if (indexComputation.getBottomLevelComputation() == sortedComputations
								.get(i).getBottomLevelComputation()) {
							if (indexComputation.getTopLevelComputation() > sortedComputations
									.get(i).getTopLevelComputation()) {
								sortedComputations.add(i, indexComputation);
								break;
							}
						}
						if (i == (sortedComputations.size() - 1)) {
							sortedComputations.add(indexComputation);
							break;
						}
					}
				}
			}
		}
		sortedComputations.remove(0);
		return sortedComputations;
	}

	public void computeTopLevelIn() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		topComputation.setTopLevelIn(0);
		topComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they may be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getDestination()));
		}
		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getPrecedingCommunications()) {
				if (!this.getComputation(indexCommunication.getSource())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					if (time < this.getComputation(
							indexCommunication.getSource()).getTopLevelIn()
							+ this.getComputation(
									indexCommunication.getSource())
									.getComputationDuration()) {
						time = this.getComputation(
								indexCommunication.getSource()).getTopLevelIn()
								+ this.getComputation(
										indexCommunication.getSource())
										.getComputationDuration();
					}
				}
			}
			if (!skipComputation) {
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					time += indexCommunication.getCommunicationDuration();
				}
				computationList.get(i).setTopLevelIn(time);
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getName() + " --> t-level-in="
				// + computationList.get(i).getTopLevelIn());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getFollowingCommunications()) {
					if (this
							.getComputation(indexCommunication.getDestination()) != bottomComputation) {
						computationList.add(this
								.getComputation(indexCommunication
										.getDestination()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			if (time < this.getComputation(indexCommunication.getSource())
					.getTopLevelIn()
					+ this.getComputation(indexCommunication.getSource())
							.getComputationDuration()) {
				time = this.getComputation(indexCommunication.getSource())
						.getTopLevelIn()
						+ this.getComputation(indexCommunication.getSource())
								.getComputationDuration();
			}
		}
		bottomComputation.setTopLevelIn(time);
	}

	public void computeBottomLevelIn() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		bottomComputation.setBottomLevelIn(0);
		bottomComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they should be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getSource()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getFollowingCommunications()) {
				if (!this.getComputation(indexCommunication.getDestination())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					int sumIn = this.getComputation(
							indexCommunication.getDestination())
							.getBottomLevelIn();
					for (CommunicationDescriptor inputCommunication : this
							.getComputation(indexCommunication.getDestination())
							.getPrecedingCommunications()) {
						sumIn += inputCommunication.getCommunicationDuration();
					}
					if (time < sumIn) {
						time = sumIn;
					}
				}
			}
			if (!skipComputation) {
				computationList.get(i).setBottomLevelIn(
						time + computationList.get(i).getComputationDuration());
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getId() + " --> b-level-in="
				// + computationList.get(i).getBottomLevelIn());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					if (this.getComputation(indexCommunication.getSource()) != topComputation) {
						computationList
								.add(this.getComputation(indexCommunication
										.getSource()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			if (time < this.getComputation(indexCommunication.getDestination())
					.getBottomLevelIn()) {
				time = this.getComputation(indexCommunication.getDestination())
						.getBottomLevelIn();
			}
		}
		topComputation.setBottomLevelIn(time);
	}

	public Vector<ComputationDescriptor> sortComputationsByBottomLevelIn() {
		Vector<ComputationDescriptor> sortedComputations = new Vector<ComputationDescriptor>();
		computeTopLevelIn();
		computeBottomLevelIn();
		sortedComputations.add(topComputation);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				for (int i = 0; i < sortedComputations.size(); i++) {
					if (indexComputation.getBottomLevelIn() > sortedComputations
							.get(i).getBottomLevelIn()) {
						sortedComputations.add(i, indexComputation);
						break;
					} else {
						if (indexComputation.getBottomLevelIn() == sortedComputations
								.get(i).getBottomLevelIn()) {
							if (indexComputation.getTopLevelIn() > sortedComputations
									.get(i).getTopLevelIn()) {
								sortedComputations.add(i, indexComputation);
								break;
							}
						}
						if (i == (sortedComputations.size() - 1)) {
							sortedComputations.add(indexComputation);
							break;
						}
					}
				}
			}
		}
		sortedComputations.remove(0);
		return sortedComputations;
	}

	public void computeTopLevelOut() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		topComputation.setTopLevelOut(0);
		topComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they may be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getDestination()));
		}
		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getPrecedingCommunications()) {
				if (!this.getComputation(indexCommunication.getSource())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					int sumOut = this.getComputation(
							indexCommunication.getSource()).getTopLevelOut()
							+ this.getComputation(
									indexCommunication.getSource())
									.getComputationDuration();
					for (CommunicationDescriptor outputCommunication : this
							.getComputation(indexCommunication.getSource())
							.getFollowingCommunications()) {
						sumOut += outputCommunication
								.getCommunicationDuration();
					}
					if (time < sumOut) {
						time = sumOut;
					}
				}
			}
			if (!skipComputation) {
				computationList.get(i).setTopLevelOut(time);
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getName() + " --> t-level-out="
				// + computationList.get(i).getTopLevelOut());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getFollowingCommunications()) {
					if (this
							.getComputation(indexCommunication.getDestination()) != bottomComputation) {
						computationList.add(this
								.getComputation(indexCommunication
										.getDestination()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			if (time < this.getComputation(indexCommunication.getSource())
					.getTopLevelOut()
					+ this.getComputation(indexCommunication.getSource())
							.getComputationDuration()) {
				time = this.getComputation(indexCommunication.getSource())
						.getTopLevelOut()
						+ this.getComputation(indexCommunication.getSource())
								.getComputationDuration();
			}
		}
		bottomComputation.setTopLevelOut(time);
	}

	public void computeBottomLevelOut() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		bottomComputation.setBottomLevelOut(0);
		bottomComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they should be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getSource()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getFollowingCommunications()) {
				if (!this.getComputation(indexCommunication.getDestination())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					if (time < this.getComputation(
							indexCommunication.getDestination())
							.getBottomLevelOut()) {
						time = this.getComputation(
								indexCommunication.getDestination())
								.getBottomLevelOut();
					}
				}
			}
			if (!skipComputation) {
				for (CommunicationDescriptor outputCommunication : computationList
						.get(i).getFollowingCommunications()) {
					time += outputCommunication.getCommunicationDuration();
				}
				computationList.get(i).setBottomLevelOut(
						time + computationList.get(i).getComputationDuration());
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getId() + " --> b-level-out="
				// + computationList.get(i).getBottomLevelOut());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					if (this.getComputation(indexCommunication.getSource()) != topComputation) {
						computationList
								.add(this.getComputation(indexCommunication
										.getSource()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			if (time < this.getComputation(indexCommunication.getDestination())
					.getBottomLevelOut()) {
				time = this.getComputation(indexCommunication.getDestination())
						.getBottomLevelOut();
			}
		}
		topComputation.setBottomLevelOut(time);
	}

	public Vector<ComputationDescriptor> sortComputationsByBottomLevelOut() {
		Vector<ComputationDescriptor> sortedComputations = new Vector<ComputationDescriptor>();
		computeTopLevelOut();
		computeBottomLevelOut();
		sortedComputations.add(topComputation);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				for (int i = 0; i < sortedComputations.size(); i++) {
					if (indexComputation.getBottomLevelOut() > sortedComputations
							.get(i).getBottomLevelOut()) {
						sortedComputations.add(i, indexComputation);
						break;
					} else {
						if (indexComputation.getBottomLevelOut() == sortedComputations
								.get(i).getBottomLevelOut()) {
							if (indexComputation.getTopLevelOut() > sortedComputations
									.get(i).getTopLevelOut()) {
								sortedComputations.add(i, indexComputation);
								break;
							}
						}
						if (i == (sortedComputations.size() - 1)) {
							sortedComputations.add(indexComputation);
							break;
						}
					}
				}
			}
		}
		sortedComputations.remove(0);
		return sortedComputations;

	}

	public void computeTopLevelInOut() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		topComputation.setTopLevelInOut(0);
		topComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they may be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getDestination()));
		}
		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getPrecedingCommunications()) {
				if (!this.getComputation(indexCommunication.getSource())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					int sumOut = this.getComputation(
							indexCommunication.getSource()).getTopLevelInOut()
							+ this.getComputation(
									indexCommunication.getSource())
									.getComputationDuration();
					for (CommunicationDescriptor outputCommunication : this
							.getComputation(indexCommunication.getSource())
							.getFollowingCommunications()) {
						sumOut += outputCommunication
								.getCommunicationDuration();
					}
					sumOut -= indexCommunication.getCommunicationDuration();
					if (time < sumOut) {
						time = sumOut;
					}
				}

			}
			if (!skipComputation) {
				for (CommunicationDescriptor inputCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					time += inputCommunication.getCommunicationDuration();
				}
				computationList.get(i).setTopLevelInOut(time);
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getName() + " --> t-level-inout="
				// + computationList.get(i).getTopLevelInOut());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getFollowingCommunications()) {
					if (this
							.getComputation(indexCommunication.getDestination()) != bottomComputation) {
						computationList.add(this
								.getComputation(indexCommunication
										.getDestination()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			if (time < this.getComputation(indexCommunication.getSource())
					.getTopLevelInOut()
					+ this.getComputation(indexCommunication.getSource())
							.getComputationDuration()) {
				time = this.getComputation(indexCommunication.getSource())
						.getTopLevelInOut()
						+ this.getComputation(indexCommunication.getSource())
								.getComputationDuration();
			}
		}
		bottomComputation.setTopLevelInOut(time);
	}

	public void computeBottomLevelInOut() {
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			indexComputation.clearReady();
		}
		bottomComputation.setBottomLevelInOut(0);
		bottomComputation.setReady();

		// Vector is very important to contain some computations multiple
		// times in it because they should be treated multiple times.
		Vector<ComputationDescriptor> computationList = new Vector<ComputationDescriptor>();
		for (CommunicationDescriptor indexCommunication : bottomComputation
				.getPrecedingCommunications()) {
			computationList.add(this.getComputation(indexCommunication
					.getSource()));
		}

		for (int i = 0; i < computationList.size(); i++) {
			int time = 0;
			boolean skipComputation = false;
			for (CommunicationDescriptor indexCommunication : computationList
					.get(i).getFollowingCommunications()) {
				if (!this.getComputation(indexCommunication.getDestination())
						.isReady()) {
					skipComputation = true;
					break;
				} else {
					int sumIn = this.getComputation(
							indexCommunication.getDestination())
							.getBottomLevelInOut();
					for (CommunicationDescriptor inputCommunication : this
							.getComputation(indexCommunication.getDestination())
							.getPrecedingCommunications()) {
						sumIn += inputCommunication.getCommunicationDuration();
					}
					sumIn -= indexCommunication.getCommunicationDuration();
					if (time < sumIn) {
						time = sumIn;
					}
				}
			}
			if (!skipComputation) {
				for (CommunicationDescriptor outputCommunication : computationList
						.get(i).getFollowingCommunications()) {
					time += outputCommunication.getCommunicationDuration();
				}
				computationList.get(i).setBottomLevelInOut(
						time + computationList.get(i).getComputationDuration());
				computationList.get(i).setReady();
				// System.out.println("step " + i + ": computationId="
				// + computationList.get(i).getId() + " --> b-level-inout="
				// + computationList.get(i).getBottomLevelInOut());
				for (CommunicationDescriptor indexCommunication : computationList
						.get(i).getPrecedingCommunications()) {
					if (this.getComputation(indexCommunication.getSource()) != topComputation) {
						computationList
								.add(this.getComputation(indexCommunication
										.getSource()));
					}
				}
			}
		}
		int time = 0;
		for (CommunicationDescriptor indexCommunication : topComputation
				.getFollowingCommunications()) {
			if (time < this.getComputation(indexCommunication.getDestination())
					.getBottomLevelInOut()) {
				time = this.getComputation(indexCommunication.getDestination())
						.getBottomLevelInOut();
			}
		}
		topComputation.setBottomLevelInOut(time);
	}

	public Vector<ComputationDescriptor> sortComputationsByBottomLevelInOut() {
		Vector<ComputationDescriptor> sortedComputations = new Vector<ComputationDescriptor>();
		computeTopLevelInOut();
		computeBottomLevelInOut();
		sortedComputations.add(topComputation);
		for (ComputationDescriptor indexComputation : ComputationDescriptorBuffer
				.values()) {
			if ((indexComputation != topComputation)
					&& (indexComputation != bottomComputation)) {
				for (int i = 0; i < sortedComputations.size(); i++) {
					if (indexComputation.getBottomLevelInOut() > sortedComputations
							.get(i).getBottomLevelInOut()) {
						sortedComputations.add(i, indexComputation);
						break;
					} else {
						if (indexComputation.getBottomLevelInOut() == sortedComputations
								.get(i).getBottomLevelInOut()) {
							if (indexComputation.getTopLevelInOut() > sortedComputations
									.get(i).getTopLevelInOut()) {
								sortedComputations.add(i, indexComputation);
								break;
							}
						}
						if (i == (sortedComputations.size() - 1)) {
							sortedComputations.addElement(indexComputation);
							break;
						}
					}
				}
			}
		}
		sortedComputations.remove(0);
		return sortedComputations;

	}

}
