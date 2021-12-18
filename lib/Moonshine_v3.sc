// v3 builds on the decoupling in v2 to introduce a useful class hierarchy:
// a base engine class which performs all the boilerplate engine stuff,
// requiring the derived class to just provide the synthdef itself.

CroneEngineOneshot : CroneEngine {
	var paramValues;
	var paramKeys;

	alloc {
		var server = Server.default;
		var controlNames;

		var def = this.class.voiceDef;

		def.send(server);
		controlNames = def.allControlNames.select({ arg ctl; ctl.name != \out && ctl.name != \freq });
		paramValues = Dictionary.with(*controlNames.collect({ arg ctl;
			ctl.name -> def.controls[ctl.index]
		}));

		paramValues.keys.do({ |key|
			this.addCommand(key, "f", { arg msg;
				paramValues[key] = msg[1];
			});
		});

		this.addCommand("hz", "f", { arg msg;
			var args = [\freq, msg[1]] ++ paramValues.getPairs;
			Synth.new(\Moonshine, args);
		});

		paramKeys = Array.newClear(controlNames.size);
		controlNames.do({ arg ctl, idx; paramKeys[idx] = ctl.name });
		
		this.addCommand("voice", Array.fill(paramKeys.size+1, {$f}).asString, { arg msg;
			var args = [\freq, msg[1]];
			paramKeys.size.do({ arg idx; args = args ++ [paramKeys[idx], msg[idx+2]]});
			Synth.new(\Moonshine, args);
		});
	}
}

Engine_Moonshine_v3 : CroneEngineOneshot {
	*voiceDef {
		^SynthDef(\Moonshine, {
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

		})
	}
}
