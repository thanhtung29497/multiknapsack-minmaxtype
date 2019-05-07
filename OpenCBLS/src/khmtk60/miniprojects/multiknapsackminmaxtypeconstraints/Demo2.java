package khmtk60.miniprojects.multiknapsackminmaxtypeconstraints;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import khmtk60.miniprojects.multiknapsackminmaxtypeconstraints.model.MinMaxTypeMultiKnapsackInput;
import khmtk60.miniprojects.multiknapsackminmaxtypeconstraints.model.MinMaxTypeMultiKnapsackInputBin;
import khmtk60.miniprojects.multiknapsackminmaxtypeconstraints.model.MinMaxTypeMultiKnapsackInputItem;

public class Demo2 {
	
	public static int LAMBDA = 1;
	public static int ITERATION = 1000;
	
	private class Bin {
		public final double w;
		public final double lw;
		public final double p;
		public final int t;
		public int index;
		public final int r;
		private double currentW = 0;
		private double currentP = 0;
		public double violation = 0;
		public ArrayList<Item> items = new ArrayList<>();
		public ArrayList<Item> candidateItems = new ArrayList<>();
		private int tNum[];
		private int rNum[];
		private int currentT = 0;
		private int currentR = 0;
		public Bin(int index, double w, double lw, double p, int t, int r, int mt, int mr) {
			this.index = index;
			this.w = w;
			this.lw = lw;
			this.p = p;
			this.t = t;
			this.r = r;
			this.tNum = new int[mt];
			this.rNum = new int[mr];
		}
		@Override
		public Bin clone() {
			Bin cloned = new Bin(index, w, lw, p, t, r, mt, mr);
			cloned.items = items;
			cloned.currentP = currentP;
			cloned.currentR = currentR;
			cloned.currentT = currentT;
			cloned.currentW = currentW;
			cloned.violation = violation;
			return cloned;
		}
		@Override 
		public String toString() {
			String result = "[";
			for (Item item: items) {
				result += item.toString() + ", ";
			}
			result += "]";
			return result;
		}
		public boolean violateIfAddItem(Item item) {
			if (tNum[item.t] == 0 && currentT >= t) {
				return true;
			}
			if (rNum[item.r] == 0 && currentR >= r) {
				return true;
			}
			if (item.w + currentW > w) {
				return true;
			}
			if (item.p + currentP > p) {
				return true;
			}
			return false;
		}
		public void addItem(Item item) {
			items.add(item);
			currentW += item.w;
			currentP += item.p;
			tNum[item.t]++;
			if (tNum[item.t] == 1)  {
				currentT++;
			}
			rNum[item.r]++;
			if (rNum[item.r] == 1)  {
				currentR++;
			}
			this.updateViolation();
		}
		public void removeItem(Item item) {
			items.remove(item);
			currentW -= item.w;
			currentP -= item.p;
			tNum[item.t]--;
			if (tNum[item.t] == 0)  {
				currentT--;
			}
			rNum[item.r]--;
			if (rNum[item.r] == 0)  {
				currentR--;
			}
			this.updateViolation();
		}
		public double getW() {
			return this.currentW;
		}
		public double getP() {
			return this.currentP;
		}
		private void updateViolation() {
			violation = 0;
			if (currentW > 0 && currentW < lw) {
				violation += Math.abs(lw - currentW);
			}
			
			if (currentW > w) {
				violation += Math.abs(w - currentW);
			}
			
			if (currentP > p) {
				violation += Math.abs(p - currentP) * LAMBDA;
			}
			
			if (currentR > r) {
				violation += Math.abs(r - currentR) * LAMBDA;
			}
			
			if (violation > 0) {
				violation += items.size();
			}

		}
		
	}
	
	private class Item {
		public final double w;
		public final double p;
		public final int t;
		public final int r;
		public final int index;
		public Bin bin = null;
		public ArrayList<Bin> bins = new ArrayList<>();
		public boolean in(Bin bin) {
			if (this.bin == null) {
				return false;
			}
			return this.bin.index == bin.index;
		}
		public Item (int index, double w, double p, int t, int r) {
			this.index = index;
			this.w = w;
			this.p = p;
			this.t = t;
			this.r = r;
		}
		@Override 
		public String toString() {
			return this.index + "";
		}
	}
	
	int n, m;
	Item items[];
	Bin bins[];
	double violations;
	double bestViolations = Double.MAX_VALUE;
	int mt, mr;
	MinMaxTypeMultiKnapsackInput input;
	ArrayList<Bin> result = new ArrayList<>();
	
	public void readInput(String fileName) {
		input = new MinMaxTypeMultiKnapsackInput();
		String directory = new File(".").getAbsolutePath() + "/bin/khmtk60/miniprojects/multiknapsackminmaxtypeconstraints";
		input = input.loadFromFile(directory + "/" + fileName);
		n = input.getItems().length;
		m = input.getBins().length;
		mt = 0;
		mr = 0;
		for (MinMaxTypeMultiKnapsackInputItem item: input.getItems()) {
			mt = Math.max(mt, item.getT() + 1);
			mr = Math.max(mr, item.getR() + 1);
		}
		
		this.bins = new Bin[m];
		this.items = new Item[n];
		
		for (int i = 0; i < m; ++i) {
			MinMaxTypeMultiKnapsackInputBin bin = input.getBins()[i];
			bins[i] = new Bin(i, bin.getCapacity(), bin.getMinLoad(), bin.getP(), bin.getT(), bin.getR(), mt, mr);
		}
		
		for (int i = 0; i < n; ++i) {
			MinMaxTypeMultiKnapsackInputItem item = input.getItems()[i];
			items[i] = new Item(i, item.getW(), item.getP(), item.getT(), item.getR());
			for (int binId: item.getBinIndices()) {
				bins[binId].candidateItems.add(items[i]);
				items[i].bins.add(bins[binId]);
			}
		}
		
		
	}
		
	public void initialize() {
		violations = 0;
		
		for (Bin bin: this.bins) {
			
			int sumW = 0;
			for (Item item: bin.candidateItems) {
				sumW += item.w;
			}
			if (sumW < bin.lw) {
				continue;
			}
			
			int j = 0;
			while (j < bin.candidateItems.size()) {
				Item item = bin.candidateItems.get(j);
				if (item.bin == null && !bin.violateIfAddItem(item)) {
					this.assignValue(bin.candidateItems.get(j), bin);
				}
				j++;
			}
		}
		this.saveResult();
	}
	
	private void saveResult() {
		if (this.violations < this.bestViolations) {
			this.bestViolations = this.violations;
			result = new ArrayList<>();
			for (Bin bin: bins) {
				result.add(bin.clone());
			}
		}
	}
	
	public Bin chooseBin() {
		ArrayList<Bin> violatedBins = new ArrayList<>();
		for (Bin bin: bins) {
			if (bin.violation > 0) {
//				maxViolation = bin.violation;
				violatedBins.add(bin);
			}
		}
		return violatedBins.get(new Random().nextInt(violatedBins.size()));
	}
	
	public double getAssignDelta(Item item, Bin newBin) {
		Bin oldBin = item.bin;
		this.assignValue(item, newBin);
		double tempViolations = this.violations;
		this.assignValue(item, oldBin);
		return tempViolations;
	}
	
	public void assignValue(Item item, Bin newBin) {
		Bin oldBin = item.bin;
		item.bin = newBin;
		
		if (oldBin != null) {
			violations -= oldBin.violation;
			oldBin.removeItem(item);
			violations += oldBin.violation;
		}
		
		if (newBin != null) {
			violations -= newBin.violation;
			newBin.addItem(item);
			violations += newBin.violation;
		}

	}
	
	public void printOutput() {
		
		int itemNumber = 0;
		for (Bin bin: result) {
			
			if (!bin.items.isEmpty() && bin.violation >= 0) {
				itemNumber += bin.items.size();
				System.out.print("Bin " + bin.index + ": ");
				System.out.print("S = " + bin.violation + ", "
						+ "w = " + bin.getW() + "/" + bin.lw + "->" + bin.w 
						+ ", p = " + bin.getP() + "/" + bin.p
						+ ", t = " + bin.currentT + "/" + bin.t
						+ ", r = " + bin.currentR + "/" + bin.r + ": ");
				for (Item item: bin.items) {
					System.out.print(item.index + ", ");
				}
				System.out.println();
			}
			
		}
		
		System.out.println("Total item: " + itemNumber);
		System.out.println("Violations: " + this.bestViolations);
		
	}
	
	public void search() {
		
		Random rand = new Random();
		
		System.out.println("S=" + this.violations);
		
		int it = 0;
		while (it < ITERATION && violations > 0) {
			it++;
			Bin chosenBin = this.chooseBin();
			Item chosenItem;
			
			double minViolations = Double.MAX_VALUE;
			ArrayList<Item> candidate = new ArrayList<>();
			for (Item item: chosenBin.candidateItems) {
				if (item.in(chosenBin)) {
					continue;
				}
				
				double tempViolations = this.getAssignDelta(item, chosenBin);
				if (tempViolations < minViolations) {
					candidate = new ArrayList<>(Arrays.asList(item));
					minViolations = tempViolations;
				} else if (tempViolations == minViolations) {
					candidate.add(item);
				}
			}
			
			if (!candidate.isEmpty() && minViolations - this.violations < 5) {
				chosenItem = candidate.get(rand.nextInt(candidate.size()));
				this.assignValue(chosenItem, chosenBin);
				System.out.println("Step " + it + ": S = " + this.violations + ", x[" + chosenItem.index + "] = " + chosenBin.index);
				this.saveResult();
			}
			
		}
		
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Demo2 demo = new Demo2();
		demo.readInput("MinMaxTypeMultiKnapsackInput.json");
		demo.initialize();
		demo.search();
		demo.printOutput();
	}

}
