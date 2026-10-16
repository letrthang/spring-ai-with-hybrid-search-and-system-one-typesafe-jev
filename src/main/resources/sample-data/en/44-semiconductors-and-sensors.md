# Physics basics — semiconductors and sensors

## What a semiconductor is
A semiconductor is a material whose electrical conductivity lies between that of a conductor and an insulator. Silicon
is the most common: pure silicon conducts poorly, but its conductivity rises sharply with temperature, light or added
impurities. That controllability makes semiconductors the basis of diodes, transistors, solar cells and computer
chips.

## Doping: n-type and p-type
Doping adds tiny amounts of impurity atoms to a semiconductor to change how it conducts. Adding phosphorus to silicon
gives extra free electrons, making n-type material; adding boron creates "holes" that act as positive charges, making
p-type material. Even one impurity atom per million silicon atoms changes the conductivity enormously.

## Diodes
A diode lets current flow easily in one direction only, from the p side to the n side of a p–n junction. A silicon
diode needs about 0.7 V across it before it conducts in the forward direction, and it blocks current in reverse.
Diodes turn AC into DC in power supplies, a process called rectification.

## Light-emitting diodes
A light-emitting diode (LED) gives out light when current flows through it, as electrons drop into holes and release
energy as photons. The colour depends on the semiconductor material: red LEDs need about 2 V and blue ones about 3 V.
An LED must be used with a series resistor that limits its current, often to about 20 mA.

## Transistors
A transistor is a semiconductor switch or amplifier, in which a small signal at one terminal controls a much larger
current between the other two. In a MOSFET, a voltage on the gate switches the current between source and drain. A
modern computer processor contains billions of transistors switching on and off billions of times a second.

## Thermistors
A thermistor is a resistor whose resistance changes strongly with temperature. In the common NTC type, resistance
falls as temperature rises: a typical 10 kΩ thermistor at 25 °C drops to about 3 kΩ at 60 °C. Thermistors measure
temperature in digital thermometers, fridges, battery chargers and car engines.

## Light-dependent resistors
A light-dependent resistor (LDR) has a resistance that falls as the light on it gets brighter. In darkness an LDR may
have a resistance of around 1 MΩ, dropping to a few hundred ohms in bright light. Wired into a voltage divider, it
gives an output voltage that changes with light level, which is used to switch street lights on at dusk.

## Photodiodes
A photodiode is a diode that produces a current when light falls on its p–n junction, because photons free electrons
and holes. The current is proportional to the light intensity and responds within nanoseconds. Photodiodes read
optical discs, receive signals from fibre-optic cables, and measure light in cameras and pulse oximeters.

## Strain gauges
A strain gauge is a thin zig-zag of metal foil glued onto a surface, whose resistance rises slightly when it is
stretched. The change is tiny, about 0.2% for a strain of 0.1%, so it is measured with a bridge circuit. Strain gauges
are the sensing element inside electronic kitchen scales, bathroom scales and industrial load cells.

## Analogue-to-digital conversion
An analogue-to-digital converter (ADC) turns a sensor's continuous voltage into a number that a computer can process.
A 10-bit ADC splits its input range into 2¹⁰ = 1,024 steps, so with a 5 V range each step is about 4.9 mV. More bits
give finer resolution: a 12-bit ADC has 4,096 steps.
