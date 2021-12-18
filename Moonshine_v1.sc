Engine_Moonshine_v1 : CroneEngine {
// All norns engines follow the 'Engine_MySynthName' convention above

	// Here, we establish variables for our synth, with starting values,
	//  which our script commands can modify:
	var freq = 330;
	var sub_div = 2;
	var noise_level = 0.1;
	var cutoff = 8000;
	var resonance = 3;
	var attack = 0;
	var release = 0.4;
	var amp = 1;
	var pan = 0;
	var out = 0;

// This is your constructor. The 'context' arg is a CroneAudioContext.
// It provides input and output busses and groups.
// NO NEED TO MODIFY THIS
	*new { arg context, doneCallback;
		^super.new(context, doneCallback);
	}

// This is called when the engine is actually loaded by a script.
// You can assume it will be called in a Routine,
//  and you can use .sync and .wait methods.
	alloc { // allocate memory to the following:

		// add SynthDefs
		SynthDef("Moonshine", {
			arg freq = freq, sub_div = sub_div, noise_level = noise_level,
			cutoff = cutoff, resonance = resonance,
			attack = attack, release = release,
			amp = amp, pan = pan, out = out;

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

		context.server.sync; // syncs our synth definition to the server

// This is how you add "commands",
//  which are how the Lua interpreter controls the engine.
// The format string is analogous to an OSC message format string,
//  and the 'msg' argument contains data.

		this.addCommand("amp", "f", { arg msg;
			amp = msg[1];
		});

		this.addCommand("sub_div", "f", { arg msg;
			sub_div = msg[1];
		});

		this.addCommand("noise_level", "f", { arg msg;
			noise_level = msg[1];
		});

		this.addCommand("cutoff", "f", { arg msg;
			cutoff = msg[1];
		});

		this.addCommand("resonance", "f", { arg msg;
			resonance = msg[1];
		});

		this.addCommand("attack", "f", { arg msg;
			attack = msg[1];
		});

		this.addCommand("release", "f", { arg msg;
			release = msg[1];
		});

		this.addCommand("pan", "f", { arg msg;
			pan = msg[1];
		});

		this.addCommand("hz", "f", { arg msg;
			Synth("Moonshine", [
				\freq,msg[1],
				\amp,amp,
				\sub_div,sub_div,
				\noise_level,noise_level,
				\cutoff,cutoff,
				\resonance,resonance,
				\attack,attack,
				\release,release,
				\pan,pan,
				\out,out
			]);
		});

	}

}