# Biotech basics — bioreactors and scale-up

## Parts of a stirred-tank bioreactor
A stirred-tank bioreactor is built from a vessel with a cooling jacket, an impeller on a central shaft for mixing, a
sparger at the bottom that releases air or oxygen bubbles, and ports for probes, feeds and sampling. Stirred tanks
are the most common design for growing cells at industrial scale. Microbial stirred tanks can exceed 100,000 L, while
stirred tanks for mammalian cells usually hold up to about 20,000 L.

## Batch, fed-batch and continuous culture
Bioreactors run in three main modes: batch, fed-batch and continuous. In batch culture all nutrients are added at the
start and nothing is added until harvest, while in fed-batch culture concentrated feed is added during the run so
that nutrients do not run out. In continuous culture fresh medium flows in and spent broth flows out at the same
rate, holding the cells in a steady state for weeks.

## Perfusion culture
Perfusion culture is a continuous mode in which spent medium is removed while the cells are kept inside the
bioreactor. A cell-retention device, such as an alternating tangential flow (ATF) filter, holds back the cells, and
typically 1–2 vessel volumes of fresh medium are exchanged per day. Perfusion reaches 50–100 million cells per mL,
several times more than the 10–30 million cells per mL typical of a CHO fed-batch culture.

## Single-use bioreactors
A single-use bioreactor uses a sterile, pre-assembled plastic bag inside a rigid support instead of a steel tank.
Because the bag is thrown away after each run, there is no cleaning or steam sterilisation between batches, which
shortens changeover time and lowers the risk of cross-contamination. Single-use bioreactors are common for mammalian
cell culture up to about 2,000 L, but the bags add consumable cost and plastic waste.

## The seed train
The seed train is the series of steps that grows enough cells to start a large production bioreactor. Cells from a
frozen vial are expanded in shake flasks and then in progressively larger bioreactors, each step usually 5 to 10
times bigger than the one before. Reaching a 10,000 L production bioreactor from a 1 mL vial of CHO cells can take
three to four weeks.

## Why scale-up is difficult
Scale-up is difficult because volume and surface area grow at different rates as a bioreactor gets bigger. When the
tank diameter increases 10-fold, the volume rises about 1,000-fold but the wall area only 100-fold, so removing heat
through the jacket becomes harder. Mixing time also grows from a few seconds in a 2 L lab vessel to a minute or more
in a 10,000 L tank, so cells near the feed point can meet much higher nutrient or base concentrations than cells far
away.

## kLa and oxygen transfer
kLa, the volumetric oxygen transfer coefficient, describes how quickly oxygen moves from gas bubbles into the liquid
of a bioreactor and is measured in h⁻¹. The oxygen transfer rate equals kLa multiplied by the gap between the
saturated and the actual dissolved oxygen concentration, written OTR = kLa × (C* − C). kLa rises with faster
stirring, more gas flow, smaller bubbles or pure oxygen, and keeping kLa similar is a common scale-up rule.

## Power input and shear
Power input per volume is a key scale-up parameter that links mixing to the mechanical stress felt by cells.
Microbial fermentations often use 1–5 kW per m³ of broth, while mammalian cells, which lack a cell wall, are usually
grown at about 10–100 W/m³. Engineers also limit the impeller tip speed, often to below about 2 m/s for animal cells,
and add surfactants such as Poloxamer 188 to protect cells from bursting bubbles.

## Heat removal
Heat removal becomes a limiting factor as fermentations get larger and denser. Fast-growing microbes such as E. coli
at high cell density release a lot of metabolic heat, and the stirrer adds more, while the cooling jacket area grows
more slowly than the volume. Large microbial bioreactors therefore add internal cooling coils or use colder cooling
water to hold the temperature within about ±0.5 °C of the set point.

## Monitoring and control
A production bioreactor is controlled by a computer that reads sensors and adjusts the culture in real time. Standard
probes measure temperature, pH, dissolved oxygen and pressure, and off-gas analysers measure O2 and CO2 to estimate
how fast the cells respire. The control system adds base or CO2 gas to hold the pH, changes stirring and gas flow to
hold dissolved oxygen, and logs every value in the batch record required by GMP rules.
