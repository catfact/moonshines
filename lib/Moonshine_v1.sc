// v1: minor cleanups

// these changes remove a little bit of clutter and introduce important language constructs:
// - iteration
// - associative collections

// but overall structure is unmodified.

Engine_Moonshine_v1 : CroneEngine {
	var params;

    // NB: don't actually need to override base class constructor

	alloc {
		SynthDef("Moonshine", {
			// NB: default args here were not doing anything, b/c always specified at synth creation
			arg out=0,
			amp, pan,
			freq, sub_div, noise_level,
			cutoff, resonance,
			attack, release;

			var pulse = Pulse.ar(freq: freq);
			var saw = Saw.ar(freq: freq);
			var sub = Pulse.ar(freq: freq/sub_div);
			var noise = WhiteNoise.ar(mul: noise_level);
			var mix = Mix.ar([pulse,saw,sub,noise]);

			var envelope = Env.perc(attackTime: attack, releaseTime: release, level: amp).kr(doneAction: 2);
			var filter = MoogFF.ar(in: mix, freq: cutoff * envelope, gain: resonance);

			var signal = Pan2.ar(filter*envelope,pan);

			Out.ar(out,signal);
		}).add;

        // create an Dictionary (an unordered associative collection)
        // to store parameter values, initialized to defaults.
		params = Dictionary.newFrom([
			\sub_div, 2,
			\noise_level, 0.1,
			\cutoff, 8000,
			\resonance, 3,
			\attack, 0,
			\release, 0.4,
			\amp, 0.5,
			\pan, 0;
		]);

        // loop over the keys of the dictionary, 
        // add a command for which one which updates corresponding value
		params.keys.do({ arg key;
			this.addCommand(key, "f", { arg msg;
				params[key] = msg[1];
			});
		});

        // the hz command. new syntax:
        // .getPairs flattens the dictionary to alternating k,v array
        // ++ concatenates arrays
		this.addCommand("hz", "f", { arg msg;
			Synth.new("Moonshine", [\freq, msg[1]] ++ params.getPairs)
		});

        // NB: we don't need to sync with the server in this function,
        // b/c were not actually doing anything that depends on the synthdef being available.
        // (the caller may want to wait before signalling that engine construction is complete,
        // but this needn't be our responsibility.)
        // Server.default.sync;

	}

}