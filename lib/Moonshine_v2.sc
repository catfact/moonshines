// v2 deploys a few more tricks to totally eliminate repetitive declarations of control/parameter/command names.
// everything is extracted from data in the synthdef itself.

Engine_Moonshine_v2 : CroneEngine {
	var paramValues;
	var paramKeys;

	alloc {
		var server = Server.default;
		var controlNames;

		var def = SynthDef(\Moonshine, {
			arg out=0,
			freq = 330,
			sub_div = 2,
			noise_level = 0.1,
			cutoff = 8000,
			resonance = 3,
			attack = 0,
			release = 0.4,
			amp = 1,
			pan = 0;

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

		// these lines produce a Dictionary mapping modulatable control names to values,
		// where the values are populated with the defaults specified above.
		// (.collect/.select are equivalent to .map/.filter in some other languages)
		//// a ControlName contains both a symbolic key and a positional index
		//// \out and \hz are controlled separately, so this line filters them out
		controlNames = def.allControlNames.select({ arg ctl; 
		    (ctl.name != \out) && (ctl.name != \freq) 
		});
		//// this line construct a dictionary by first associating ControlName keys to default values from the def
		paramValues = Dictionary.with(*controlNames.collect({ arg ctl;
			ctl.name -> def.controls[ctl.index]  // this syntax creates an Association
		}));
		
		paramValues.keys.do({ arg key;
			this.addCommand(key, "f", { arg msg;
				paramValues[key] = msg[1];
			});
		});
		
		this.addCommand("hz", "f", { arg msg;
			var args = [\freq, msg[1]] ++ paramValues.getPairs;
			Synth.new(\Moonshine, args);
		});

		// add an additional parameter accepting all control values; in order for this to work,
		// we need a separate record of the correct order of control names.
		paramKeys = Array.newClear(controlNames.size);
		controlNames.do({ arg ctl, idx; paramKeys[idx] = ctl.name });
		this.addCommand("voice", Array.fill(paramKeys.size+1, {$f}).asString, { arg msg;
			var args = [\freq, msg[1]];
			paramKeys.size.do({ arg idx; args = args ++ [paramKeys[idx], msg[idx+2]]});
			Synth.new(\Moonshine, args);
		});

	}

}