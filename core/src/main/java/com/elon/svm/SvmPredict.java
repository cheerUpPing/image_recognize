package com.elon.svm;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantLock;

public class SvmPredict {
	private static HashMap<String, svm_model> svmModel = new HashMap<>();
	private static ReentrantLock msLock = new ReentrantLock();

	private static double atof(String s) {
		return Double.valueOf(s).doubleValue();
	}

	private static int atoi(String s) {
		return Integer.parseInt(s);
	}

	private static StringBuffer predict(StringBuffer data, svm_model model) {
		String[] dataStr = data.toString().split("\r\n");
		StringBuffer value = new StringBuffer();
		for (String line : dataStr) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");
			atof(st.nextToken());
			int m = st.countTokens() / 2;
			svm_node[] x = new svm_node[m];
			for (int j = 0; j < m; j++) {
				x[j] = new svm_node();
				x[j].index = atoi(st.nextToken());
				x[j].value = atof(st.nextToken());
			}
			int v;
			v = (int) svm.svm_predict(model, x);
			value.append(v + "\n");
			// output.writeBytes(v + "\n");
		}
		return value;
	}

	private static void exit_with_help() {
		System.out.print("usage: svm_predict [options] test_file model_file output_file\n" + "options:\n"
				+ "-b probability_estimates: whether to predict probability estimates, 0 or 1 (default 0); one-class SVM not supported yet\n");
		// System.exit(1);
	}

	public static StringBuffer main(StringBuffer data, String modelPath) {
		StringBuffer value = null;
		try {
			msLock.lock();
			svm_model model = svmModel.get(modelPath);
			if (model == null) {
				model = svm.svm_load_model(modelPath);
				svmModel.put(modelPath, model);
			}
			msLock.unlock();

			if (svm.svm_check_probability_model(model) != 0) {
				System.out.print("Model supports probability estimates, but disabled in prediction.\n");
			}

			value = predict(data, model);
		} catch (Exception e) {
			exit_with_help();
		}
		return value;
	}
}
