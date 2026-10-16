# Physics basics — electromagnetic induction and motors

## Magnetic flux
Magnetic flux Φ measures how much magnetic field passes through an area, and Φ = B · A when the field is at right
angles to the area. Magnetic flux is measured in webers (Wb), where 1 Wb = 1 T · m². A coil of area 0.01 m² in a 0.5 T
field at right angles has a magnetic flux of 0.005 Wb through each turn.

## Faraday's law in numbers
Faraday's law says the voltage induced in a coil equals the number of turns times the rate of change of magnetic flux:
emf = N · ΔΦ / Δt. If the flux through a 200-turn coil falls from 0.005 Wb to zero in 0.1 s, the induced emf is 200 ×
0.005 / 0.1 = 10 V. Changing the flux twice as fast, or using twice as many turns, doubles the induced voltage.

## Lenz's law and the falling magnet
Lenz's law says an induced current always flows in the direction that opposes the change in flux that created the
current. A strong magnet dropped down a copper pipe falls very slowly, because the falling magnet induces currents in
the pipe whose magnetic fields push back against its motion. Lenz's law follows from conservation of energy: the
energy of the induced current comes from work done against this opposing force.

## Eddy currents
Eddy currents are loops of current induced inside a solid piece of metal when the magnetic field through the metal
changes. Eddy currents heat the metal and create forces that oppose motion, an effect used in the magnetic brakes of
trains and roller coasters. In transformers and motors eddy currents waste energy, so their cores are built from thin
insulated steel sheets called laminations.

## Induction cooking
An induction hob heats a pan by electromagnetic induction rather than with a hot surface. A coil under the glass
carries an alternating current at about 20 to 100 kHz, and the changing magnetic field induces eddy currents in the
base of the pan, which heats up directly. Induction cooking needs pans made of a magnetic material such as cast iron
or magnetic stainless steel, and it delivers about 85% of the energy to the pan.

## AC generators
An AC generator, or alternator, spins a coil in a magnetic field, or spins a magnet inside fixed coils, to produce an
alternating voltage. The output of an AC generator reverses every half turn, so a simple two-pole generator turning at
3,000 revolutions per minute produces 50 Hz AC. Power-station generators, car alternators and bicycle dynamos all work
this way, and a car alternator's output is rectified to DC to charge the battery.

## Parts of a DC motor
A simple DC motor has a rotating coil called the armature, a permanent magnet or field coils around it, and a
commutator with brushes that feeds current to the spinning coil. The commutator is a split ring that reverses the
current in the armature every half turn, so the turning force keeps acting in the same direction. Brushes wear out
over time, so many modern DC motors are brushless and switch the current electronically instead.

## Back EMF
Back EMF is the voltage that a spinning motor induces in its own coils, because every motor also acts as a generator.
Back EMF opposes the supply voltage, so the current drawn is I = (V − back EMF) / R. At start-up the motor is not
turning, back EMF is zero and the current is large, which is why a starting motor can briefly dim the lights; as the
speed rises, back EMF grows and the current falls.

## Induction motors
An induction motor is an AC motor whose rotor has no electrical connections: the stator coils create a rotating
magnetic field that induces currents in the rotor, and those currents make the rotor turn. The rotor always turns
slightly slower than the field, a difference called slip, typically 2 to 5%. A four-pole induction motor on 50 Hz
mains therefore runs at about 1,450 rpm instead of the field's 1,500 rpm.

## Regenerative braking
Regenerative braking uses the motor of an electric vehicle as a generator to slow the vehicle down. When the driver
brakes, the wheels turn the motor, which induces a current that charges the battery instead of turning kinetic energy
into heat in the brake pads. Electric cars can recover up to about 70% of the braking energy this way, which extends
their range, especially in stop-and-go city driving.
