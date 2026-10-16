# RAS basics — Energy use and backup power

## Where electricity goes in a RAS
Electricity in a RAS goes mainly to water pumps, which often use 30 to 50 % of the total. Heating or cooling,
blowers for degassing and aeration, oxygen generators, UV lamps and lighting make up most of the rest. Knowing this
split shows where energy-saving efforts will pay back fastest.

## Calculating pump power
Pump power in a RAS can be estimated with the formula P = ρ · g · Q · H / η, where Q is flow, H is head and η is
pump efficiency. For example, moving 1,000 m³/h against 1.5 m of head with a 70 % efficient pump needs about 5.8
kW. Every extra metre of head adds roughly 3.9 kW at this flow, so a low-head design saves a lot of energy.

## Energy for oxygen and blowers
On-site oxygen generators using PSA typically need about 0.5 to 1 kWh of electricity per kilogram of oxygen
produced. Blowers for degassers and moving-bed biofilters run around the clock, so efficient blowers with
variable-speed drives save a lot over a year. Reducing air pressure losses in pipes and diffusers also lowers
blower power.

## Sub-metering energy use
Sub-metering means installing separate electricity meters on major loads such as pump groups, blowers, heat pumps
and oxygen generators. Sub-meters show which equipment uses the most energy and reveal problems, such as a pump
drawing more power as its impeller wears. Tracking kWh per kilogram of feed each week makes slow drift easy to
spot.

## Reducing peak demand
Peak demand is the highest power draw at any one moment, and many electricity tariffs charge extra for it. RAS
farms lower peak demand by starting large motors in stages with soft starters or frequency converters instead of
direct-on-line starting. Flexible loads such as sludge dewatering or water heating can be moved to night hours when
tariffs are lower.

## Sizing a backup generator
A backup generator for a RAS must cover all critical loads at once, including recirculation pumps, blowers, oxygen
supply, controls and alarms. Electric motors can draw 5 to 7 times their running current when they start, so the
generator is usually sized at 1.25 to 1.5 times the running load, or the loads are started in stages. Fuel storage
should last at least 24 to 72 hours.

## Automatic transfer switch
An automatic transfer switch (ATS) detects a mains power failure, starts the backup generator and moves the farm
load onto it, usually within 10 to 30 seconds. When mains power returns and stays stable, the ATS switches back and
lets the generator cool down. Pumps and blowers should restart in a set order so the generator is not overloaded.

## UPS for controls and alarms
An uninterruptible power supply (UPS) keeps the PLC, sensors, network and alarm dialler running during the gap
before the generator takes over. The UPS should also hold these systems for at least 30 to 60 minutes in case the
generator fails to start. Without a UPS, the farm would lose its alarms and data at the exact moment they are most
needed.

## How fast oxygen falls without power
Dissolved oxygen in a RAS tank falls fast when the power fails, because water flow and oxygen supply stop while the
fish keep breathing. Fish at 50 kg/m³ that use 300 mg of oxygen per kg per hour consume 15 mg/L per hour, so oxygen
can drop from 10 to 5 mg/L in about 20 minutes. That is why backup oxygen fed from a pressurised liquid oxygen tank
must work without electricity.

## Generator maintenance and fuel
A RAS backup generator needs regular maintenance, including a weekly start, a monthly run under real load and
checks of oil, coolant and starter batteries. Diesel fuel stored for months can grow microbes and collect water, so
the tank should be drained of water and the fuel tested or replaced. A full fuel tank and a contract for emergency
fuel delivery protect the farm during long power outages.
