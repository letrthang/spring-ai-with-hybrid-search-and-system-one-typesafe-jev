# Physics basics — AC power and transformers

## The AC sine wave
An alternating current (AC) supply follows a sine wave, rising to a positive peak, falling through zero to a negative
peak and back again. The period is the time for one full cycle, T = 1 / f, so 50 Hz mains has a period of 20 ms and 60
Hz mains about 16.7 ms. On 50 Hz mains the voltage passes through zero 100 times every second.

## Power in an AC circuit
For a simple resistive load such as a heater or a kettle, AC power is P = V × I, using RMS values of voltage and
current. A 2,300 W kettle on 230 V mains therefore draws 10 A RMS, and a 1,200 W hair dryer on 120 V mains draws 10 A
as well. The instantaneous power rises and falls 100 times a second on 50 Hz mains, but the average equals the RMS
product.

## Why power lines use high voltage
Power lines use high voltage because the heat lost in a cable is P = I² R, so a smaller current wastes far less
energy. Sending 100 MW at 400 kV needs a current of only 250 A, while sending the same power at 20 kV would need 5,000
A. Since the loss depends on current squared, the 20 kV line would waste 400 times more energy in the same cable.

## Step-up and step-down transformers in the grid
The electricity grid uses step-up transformers at power stations and step-down transformers near homes. A generator
typically produces about 25 kV, which a step-up transformer raises to 275 kV or 400 kV for long-distance transmission.
Substations then step the voltage down to around 33 kV and 11 kV, and a local transformer on a pole or in a street
cabinet finally supplies 230 V to houses.

## Current in an ideal transformer
In an ideal transformer the input power equals the output power, so Vp × Ip = Vs × Is. Stepping the voltage up by a
factor of 10 therefore steps the current down by a factor of 10. A transformer that turns 230 V into 23 V and delivers
5 A on its secondary coil draws only 0.5 A from the mains on its primary coil.

## Transformer losses and laminated cores
Real transformers lose energy as heat in the copper windings and in the iron core. Changing magnetic fields induce
swirling eddy currents in the core, so the core is built from thin sheets of silicon steel insulated from each other,
called laminations, which block most of those currents. Large grid transformers reach efficiencies above 99%, while
small plug-in transformers are often only 80 to 90% efficient.

## Transformer hum
Transformer hum is the low buzzing sound a transformer makes at twice the mains frequency, 100 Hz on 50 Hz mains and
120 Hz on 60 Hz mains. The hum comes from magnetostriction: the steel core stretches and shrinks very slightly each
time the magnetic field peaks, which happens twice in every AC cycle. Loose laminations and cooling fans can make the
hum louder.

## Three-phase power
Three-phase power uses three AC voltages that are shifted by 120°, or one third of a cycle, from each other. Each
phase in Europe is 230 V relative to neutral, and the voltage between two phases is 230 × √3, about 400 V. Three-phase
supply delivers steady power, runs large motors smoothly and needs less copper than three separate single-phase
circuits, so grids and factories use it.

## Power factor
Power factor is the ratio of real power, measured in watts, to apparent power, V × I, measured in volt-amperes (VA).
In motors and other inductive loads the current falls out of step with the voltage, so real power is P = V × I × cos
φ, where φ is the phase angle. A motor drawing 10 A at 230 V with a power factor of 0.8 uses 1,840 W of real power
from 2,300 VA.

## Switch-mode chargers
A switch-mode power supply, like a phone charger, changes 230 V AC mains into low-voltage DC with a very small
transformer. The charger first rectifies the mains to DC, then switches it on and off tens or hundreds of thousands of
times a second, and a tiny ferrite-core transformer steps the voltage down to 5 V. A higher frequency needs a much
smaller core, so a 20 W charger weighs far less than a 50 Hz transformer of the same power.
