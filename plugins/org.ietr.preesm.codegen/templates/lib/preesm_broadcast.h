#ifndef FORK_H
#define FORK_H

#include <systemc>

using namespace std;

template<class T, int input_size = 1, int nb_output = 2>
SC_MODULE (preesm_broadcast) {

	sc_core::sc_out<bool> enable_port;
	sc_core::sc_in<bool> invoke_port;

	sc_core::sc_fifo_in<T> in;
	sc_core::sc_fifo_out<T> outs[nb_output];
	T buffer[input_size] ;

	void enable() {
		bool isEnable;
		while (1) {
			isEnable = true;
			enable_port.write(false);
			sc_core::wait(10, sc_core::SC_NS);
			sc_core::wait(100, sc_core::SC_NS, in.data_written_event());
			isEnable &= (in.num_available() == input_size);
			enable_port.write(isEnable);
			sc_core::wait(10, sc_core::SC_NS);
		}
	}

	void invoke() {
		int i, j;
		while (1) {
			wait(invoke_port.posedge_event());
			cout << "invoking actor: " << "Broadcast" << endl;
			for (j = 0; j < input_size; j++) buffer[j] = in.read() ;
			for (i = 0; i < nb_output; i++) {
				for (j = 0; j < input_size; j++) {
					outs[i].write(buffer[j]);
				}
			}
		}
	}

	SC_CTOR(preesm_broadcast) {
		SC_THREAD(invoke);
		SC_THREAD(enable);
	}

};

#endif
