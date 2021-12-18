Engine_Moonshine_v2 : CroneEngine {
	var paramValues;
	var paramKeys;

	*new { arg context, doneCallback;
		^super.new(context, doneCallback);
	}

	alloc {
		var server = Server.default;
		var controlNames;

		// add SynthDefs
		// synthdef identifiers should be Symbols
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

		});

		def.send(server);

		// these lines produce a Dictionary mapping modulatable control names to values,
		// where the values are populated with the defaults specified above.
		// (.collect/.select are equivalent to .map/.filter in some other languages)
		//// a ControlName contains both a symbolic key and a positional index
		//// \out and \hz are controlled separately, so this line filters them out
		controlNames = def.allControlNames.select({ arg ctl; ctl.name != \out && ctl.name != \hz });
		//// this line construct a dictionary by first associating ControlName keys to default values from the def
		paramValues = Dictionary.with(*controlNames.collect({ arg ctl;
			ctl.name -> def.controls[ctl.index]  // this syntax creates an Association
		}));


		// for each "param," add an engine command,
		// which sets the current value default value of that param for new synth instances.
		paramKeys.do({ |key|
			this.addCommand(key.toString, "f", { arg msg;
				paramValues[key] = msg[1];
			});
		});

		// the command "hz" will create a new synth with the specifed frequency,
		// and all other parameters pulled from the param value collection.
		this.addCommand("hz", "f", { arg msg;
			var args = [\hz, msg[1]] ++ paramValues.getPairs;
			Synth.new(\Moonshine, args, Server.default);
		});

		// add an additional parameter accepting all control values in order
		// for this to work, we also need to maintain a record of the correct order of control names.
		paramKeys = Array.newClear(controlNames.size);
		controlNames.do({ arg ctl, idx; paramKeys[idx] = ctl.name });
		this.addCommand("voice", Array.fill(paramKeys.size+1, {$f}).asString, { arg msg;
			var args = [\hz, msg[1]];
			paramKeys.size.do({ arg idx; args = args ++ [paramKeys[idx], msg[idx+2]]});
			Synth.new(\Moonshine, args, Server.default);
		});

		// add an addition
		// might as well do this here,
		// so that the engine will not signal that it's ready before synthdefs are available.
		// (but really (TODO) this should just be done in the caller after every engine init.)
		server.sync;

	}

}