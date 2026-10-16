# Physics basics — solar cells and batteries

## How a solar cell works
A solar cell turns light directly into electricity using a junction between p-type and n-type silicon. When a
photon with enough energy is absorbed, it frees an electron and leaves a hole, and the electric field at the p-n
junction pushes them apart so a current flows through an external circuit. A single silicon solar cell gives an
open-circuit voltage of about 0.6 to 0.7 V, whatever its size.

## Solar cell efficiency
Solar cell efficiency is the percentage of incoming light power that a cell converts into electrical power.
Commercial silicon panels reach about 18 to 23 %, while the theoretical limit for a single-junction cell, known as
the Shockley–Queisser limit, is about 33 %. Multi-junction cells that stack several materials have passed 45 % in
the laboratory and are used on satellites.

## Current–voltage curve and maximum power point
The current–voltage (I–V) curve of a solar cell shows that the cell gives its largest current at short circuit and
its largest voltage at open circuit, but no power at either end. Power P = V × I peaks at the maximum power point,
typically at about 80 % of the open-circuit voltage. Charge controllers and inverters with maximum power point
tracking (MPPT) keep adjusting the load to stay at this point as sunlight changes.

## Cells in series and parallel
Solar cells are wired in series inside a panel to raise the voltage, and panels are combined in series or parallel
to reach the voltage and current a system needs. A classic panel has 60 cells in series and an open-circuit voltage
of about 37 to 40 V. Because series cells share the same current, one shaded cell can cut the output of a whole
string, so panels include bypass diodes that route current around shaded groups of cells.

## Temperature and solar panel output
Solar panels produce less power when they are hot, because the cell voltage falls as temperature rises. A
typical silicon panel loses about 0.3 to 0.5 % of its power for every degree Celsius above the 25 °C test
temperature. On a sunny roof the cells can reach 60 °C, cutting output by roughly 10 to 18 %, so good airflow
behind the panels helps.

## Peak sun hours and daily energy
Peak sun hours are the number of hours per day that would deliver the day's total sunlight energy at the standard
intensity of 1,000 W/m². A sunny location may receive 5 peak sun hours, so a 1 kW array produces about 5 kWh per
day before losses, or roughly 4 kWh after inverter, wiring and temperature losses. Annual yields range from about
900 kWh per installed kW in cloudy northern Europe to over 1,600 kWh in sunny regions.

## How a rechargeable battery works
A rechargeable battery stores energy chemically in two electrodes separated by an electrolyte that lets ions pass
but not electrons. In a lithium-ion battery, lithium ions move from the graphite anode to the metal-oxide cathode
during discharge, while electrons travel through the external circuit and power the device. Charging pushes the
lithium ions back into the graphite, ready for the next discharge.

## Common battery chemistries
Common rechargeable battery chemistries differ in cell voltage, energy density and lifetime. Lead-acid cells give
2.0 V each, so a 12 V car battery has six cells, and store only about 30 to 40 Wh/kg. Lithium-ion cells with nickel
or cobalt oxide cathodes give about 3.6 to 3.7 V and 150 to 270 Wh/kg, which suits phones and electric cars, while
lithium iron phosphate (LiFePO4) cells give 3.2 V and trade some energy density for longer life and better safety.

## C-rate and depth of discharge
The C-rate expresses charge or discharge current relative to battery capacity: for a 100 Ah battery, 1C is 100 A
and 0.5C is 50 A, which empties the battery in about 2 hours. Depth of discharge (DoD) is the fraction of capacity
used before recharging. Shallow cycling greatly extends life: a lead-acid battery lasts far longer at 50 % DoD than
at 100 %, and LiFePO4 batteries typically manage 3,000 to 6,000 cycles at 80 % DoD.

## Sizing a small solar battery system
A small off-grid solar battery is sized from the daily energy use, the days of backup wanted and the usable depth
of discharge. For example, a cabin using 2 kWh per day with 2 days of backup needs 4 kWh of usable storage, which
is 5 kWh of LiFePO4 capacity at 80 % DoD or 8 kWh of lead-acid capacity at 50 % DoD. The solar array is then sized
to refill the daily use in the darkest month, allowing for about 20 % losses.
