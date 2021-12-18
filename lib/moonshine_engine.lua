local Moonshine = {}
local Formatters = require 'formatters'

-- first, we'll collect all of our commands into norns-friendly ranges
local specs = {
  ["amp"] = controlspec.new(0, 2, "lin", 0, 1, ""),
  ["sub_div"] = controlspec.new(1, 10, "lin", 1, 2, ""),
  ["noise_level"] = controlspec.new(0, 1, "lin", 0, 0.3, ""),
  ["cutoff"] = controlspec.WIDEFREQ,
  ["resonance"] = controlspec.new(0, 4, "lin", 0, 1, ""),
  ["attack"] = controlspec.new(0.003, 8, "exp", 0, 0, "s"),
  ["release"] = controlspec.new(0.003, 8, "exp", 0, 1, "s"),
  ["pan"] = controlspec.PAN
}

-- this table establishes an order for parameter initialization:
local param_names = {"amp","sub_div","noise_level","cutoff","resonance","attack","release","pan"}

-- initialize parameters:
function Moonshine.add_params()
  params:add_group("Moonshine",#param_names)

  for i = 1,#param_names do
    local p_name = param_names[i]
    params:add{
      type = "control",
      id = "Moonshine_"..p_name,
      name = p_name,
      controlspec = specs[p_name],
      formatter = p_name == "pan" and Formatters.bipolar_as_pan_widget or nil
    }
  end

end

-- a single-purpose triggering command to gather our parameter values
--  and send them as commands before we fire a note
function Moonshine.trig(hz)

  for i = 1,#param_names do
    local p_name = param_names[i]
    local current_val = params:get("Moonshine_"..p_name)
    engine[p_name](current_val)
  end
  
  if hz ~= nil then
    engine.hz(hz)
  end
  
end

 -- we need to return these engine-specific Lua functions back to the host script:
return Moonshine