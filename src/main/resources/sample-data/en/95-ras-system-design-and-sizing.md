# RAS basics — System design and sizing

## Where RAS design starts
RAS design starts from the production target: how many tonnes of fish per year, at what harvest size and with what
growth time. These three numbers give the maximum standing biomass the farm must hold at any moment, which then
sizes every tank, pump and filter. For example, a farm holding at most 40 tonnes of fish is designed around that
40-tonne peak, not around its average stock.

## Tank volume from biomass and density
The fish tank volume of a RAS is the maximum biomass divided by the design stocking density. Holding 40,000 kg of
rainbow trout at 50 kg/m³ needs 800 m³ of tank water. Designers usually add 10 to 20 % extra tank space for
grading, quarantine and harvest batches, because fish are rarely spread perfectly evenly across the tanks.

## Design feed load
The design feed load of a RAS is the largest amount of feed the system must handle per day, calculated as peak
biomass times feeding rate. A 40-tonne stock fed 1 % of its body weight per day eats 400 kg of feed daily. Because
oxygen use, CO2, ammonia and solids all scale with feed, this single number sizes the oxygen system, degasser,
biofilter and drum filter.

## Oxygen demand
The oxygen demand of a RAS is estimated from the feed load: fish use roughly 0.3 to 0.5 kg of oxygen per kg of
feed, and the biofilter adds about 0.1 to 0.15 kg. With 400 kg of feed per day, the system must supply about 160 to
260 kg of oxygen daily. The oxygen source, such as liquid oxygen or an oxygen generator, should cover the peak
hours after feeding with a safety margin.

## Flow rate from the oxygen budget
The recirculation flow of a RAS is often calculated as the oxygen the fish need divided by the oxygen each cubic
metre of water can deliver. If fish consume 160 kg of oxygen per day, about 6.7 kg per hour, and water enters the
tanks at 15 mg/L and leaves at 8 mg/L, each cubic metre delivers 7 g. The required flow is then about 950 m³/h,
which replaces an 800 m³ tank volume roughly every 50 minutes.

## Biofilter sizing
A RAS biofilter is sized from the daily ammonia load, commonly estimated as TAN = feed × protein fraction ×
0.092. With 400 kg of feed containing 45 % protein, the fish excrete about 16.6 kg of TAN per day. At a removal
rate of 0.4 g TAN per m² per day, the media need about 41,500 m² of surface, or roughly 83 m³ of moving-bed
carriers at 500 m²/m³.

## Degasser sizing
A CO2 degasser is sized to treat most or all of the recirculating flow with enough air to keep tank CO2 below about
15 to 20 mg/L. Fish produce roughly 1.4 kg of CO2 for every kg of oxygen they consume, so 160 kg of oxygen per day
means over 200 kg of CO2 to remove. With an air-to-water ratio of 5:1 to 10:1, a flow of 950 m³/h needs fans moving
about 4,750 to 9,500 m³ of air per hour.

## Solids load and drum filter sizing
The solids load of a RAS is roughly 25 to 30 % of the feed weight as dry suspended solids, so 400 kg of feed
produces about 100 to 120 kg of solids per day. The drum filter must be rated for the full recirculating flow at
that solids load, usually with a 20 to 30 % margin. An undersized drum filter backwashes almost constantly and lets
fine solids pass on to the biofilter.

## Redundancy and backup
A RAS must be designed so that no single equipment failure can kill the fish. Common practice is N+1 pumps and
blowers, a standby generator that starts automatically, and an emergency oxygen supply that feeds diffusers in
every tank when the main system stops. Because fish can die within an hour without flow or oxygen, backup equipment
must be tested regularly, not only installed.

## Design margins and total water volume
The total water volume of a RAS is larger than its tank volume, because sumps, filters, the biofilter and pipes
typically add 20 to 40 % more. Designers also add a safety margin of about 20 to 30 % to the peak loads for oxygen,
flow and biofiltration, and leave space for future tanks. A RAS sized exactly to the calculated peak has no room
for warm weather, faster growth or equipment wear.
