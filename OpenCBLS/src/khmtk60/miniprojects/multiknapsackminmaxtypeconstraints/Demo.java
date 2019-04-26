package khmtk60.miniprojects.multiknapsackminmaxtypeconstraints;

import java.io.File;

import khmtk60.miniprojects.multiknapsackminmaxtypeconstraints.model.MinMaxTypeMultiKnapsackInput;
import khmtk60.miniprojects.multiknapsackminmaxtypeconstraints.model.MinMaxTypeMultiKnapsackInputBin;
import khmtk60.miniprojects.multiknapsackminmaxtypeconstraints.model.MinMaxTypeMultiKnapsackInputItem;
import localsearch.constraints.basic.LessOrEqual;
import localsearch.functions.basic.FuncMult;
import localsearch.functions.basic.FuncPlus;
import localsearch.functions.sum.Sum;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.MultiStageGreedySearch;
import localsearch.search.TabuSearch;
import localsearch.selectors.MinMaxSelector;

public class Demo {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		MinMaxTypeMultiKnapsackInput input = new MinMaxTypeMultiKnapsackInput();
		String directory = new File(".").getAbsolutePath() + "/bin/khmtk60/miniprojects/multiknapsackminmaxtypeconstraints";
		input = input.loadFromFile(directory + "/test.json");
		
		LocalSearchManager mgr = new LocalSearchManager();
		ConstraintSystem constraints = new ConstraintSystem(mgr);
		
		int n = input.getItems().length;
		int m = input.getBins().length;
		int mt = 0, mr = 0;
		for (MinMaxTypeMultiKnapsackInputItem item: input.getItems()) {
			mt = Math.max(mt, item.getT() + 1);
			mr = Math.max(mr, item.getR() + 1);
		}
		
		VarIntLS[][] x = new VarIntLS[n][m];
		VarIntLS[][] y = new VarIntLS[mt][m];
		VarIntLS[][] z = new VarIntLS[mr][m];
		
		for (int j = 0; j < m; ++j) {
			for (int i = 0; i < n; ++i) {
				x[i][j] = new VarIntLS(mgr, 0, 1);
			}
			for (int i = 0; i < mt; ++i) {
				y[i][j] = new VarIntLS(mgr, 0, 1);
			}
			for (int i = 0; i < mr; ++i) {
				z[i][j] = new VarIntLS(mgr, 0, 1);
			}
		}
		
		int w[] = new int[n];
		int p[] = new int[n];
		int t[] = new int[n];
		int r[] = new int[n];
		int LW[] = new int[m];
		int W[] = new int[m];
		int P[] = new int[m];
		int T[] = new int[m];
		int R[] = new int[m];
		int base = 1000;
		
		for (int i = 0; i < n; ++i) {
			MinMaxTypeMultiKnapsackInputItem item = input.getItems()[i];
			w[i] = (int)Math.floor(item.getW() * base);
			p[i] = (int)Math.floor(item.getP() * base);
			t[i] = item.getT();
			r[i] = item.getR();
		}
		
		for (int i = 0; i < m; ++i) {
			MinMaxTypeMultiKnapsackInputBin bin = input.getBins()[i];
			W[i] = (int)Math.floor(bin.getCapacity() * base);
			LW[i] = (int)Math.floor(bin.getMinLoad() * base);
			P[i] = (int)Math.floor(bin.getP() * base);
			T[i] = bin.getT();
			R[i] = bin.getR();
		}
		
		// Sum of first weight in range of bin's first weight
		for (int b = 0; b < m; ++b) {
			FuncMult[] multis = new FuncMult[n];
			for (int i = 0; i < n; ++i) {
				multis[i] = new FuncMult(x[i][b], w[i]); 
			}
			Sum sumW = new Sum(multis);
			constraints.post(new LessOrEqual(LW[b], sumW));
			constraints.post(new LessOrEqual(sumW, W[b]));
		}
		
		// Sum of second weight <= bin's second weight
		for (int b = 0; b < m; ++b) {
			FuncMult[] multis = new FuncMult[n];
			for (int i = 0; i < n; ++i) {
				multis[i] = new FuncMult(x[i][b], p[i]); 
			}
			Sum sumP = new Sum(multis);
			constraints.post(new LessOrEqual(sumP, P[b]));
		}
		
		for (int b = 0; b < m; ++b) {
			for (int i =0; i < n; ++i) {
				constraints.post(new LessOrEqual(x[i][b], y[t[i]][b]));
				constraints.post(new LessOrEqual(x[i][b], z[r[i]][b]));
			}
		}
		
		for (int b = 0; b < m; ++b) {
			VarIntLS[] Y = new VarIntLS[mt];
			VarIntLS[] Z = new VarIntLS[mr];
			for (int i = 0; i < mt; ++i) {
				Y[i] = y[i][b];
			}
			for (int i = 0; i < mr; ++i) {
				Z[i] = z[i][b];
			}
			constraints.post(new LessOrEqual(new Sum(Y), T[b]));
			constraints.post(new LessOrEqual(new Sum(Z), R[b]));
		}
		
		mgr.close();
		constraints.close();
		
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				x[i][j].setValuePropagate(1);
			}
		}
		
		TabuSearch search = new TabuSearch();
		search.search(constraints, 50, 200, 10000, 100);
		
//		MultiStageGreedySearch search = new MultiStageGreedySearch();
//		search.search(constraints, 10000, 100000, true);
		
		
//		System.out.println("Init S = " + constraints.violations());
//		MinMaxSelector mms = new MinMaxSelector(constraints);
//		
//		int it = 0;
//		while(it < 10000 && constraints.violations() > 0){
//			
//			VarIntLS sel_x = mms.selectMostViolatingVariable();
//			int sel_v = mms.selectMostPromissingValue(sel_x);
//			
//			sel_x.setValuePropagate(sel_v);
//			System.out.println("Step " + it + ", x[" + sel_x + "] := " + sel_v + ", S = " + constraints.violations());
//			
//			it++;
//		}
		
		for (int i = 0; i < n; ++i) {
			for (int j = 0; j < m; ++j) {
				System.out.println("x[" + i + "," + j + "]=" + x[i][j].getValue());
			}
		}
	}

}
