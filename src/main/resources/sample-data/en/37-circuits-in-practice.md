# Physics basics — circuits in practice

## Alternating and direct current
Direct current (DC) flows in one direction only, as from a battery or a solar panel. Alternating current (AC) reverses
direction many times a second: mains electricity alternates at 50 Hz in Europe and most of Asia and at 60 Hz in North
America. Power grids use AC because transformers can easily change its voltage.

## Mains voltage and RMS
Mains voltage is quoted as an RMS (root-mean-square) value, the steady DC voltage that would deliver the same heating
power. European 230 V mains actually peaks at about 325 V, because the peak of a sine wave is √2 times its RMS value.
US mains is 120 V RMS and peaks at about 170 V.

## Fuses and circuit breakers
A fuse is a thin wire that melts and breaks the circuit if the current gets too high, protecting the wiring from
overheating and fire. A circuit breaker does the same job with a switch that trips and can be reset. A fuse rating
should be just above the normal current, for example 13 A for a 2,500 W heater on 230 V.

## Earthing
The earth wire connects the metal case of an appliance to the ground. If a live wire comes loose and touches the case,
a large current flows straight to earth and blows the fuse or trips the breaker, instead of passing through a person
who touches it. Appliances with plastic cases may be double insulated and need no earth wire.

## Residual-current devices
A residual-current device (RCD) compares the current in the live and neutral wires and cuts the power within about 30
milliseconds if they differ. A difference of just 30 mA means current is leaking somewhere, possibly through a person.
RCDs protect against electric shock in bathrooms, kitchens and outdoor sockets.

## Short circuits
A short circuit is an unintended path of very low resistance, such as two bare wires touching, that lets a huge
current flow. Because almost nothing limits the current, the wires heat up quickly and can melt or start a fire.
Touching a spanner across a car battery's terminals can drive hundreds of amperes through it.

## Batteries and internal resistance
A battery supplies an electromotive force (EMF), but some voltage is lost inside it because of its internal
resistance. The terminal voltage is V = ε − I r, so it drops as more current is drawn. A 1.5 V cell with 0.5 Ω of
internal resistance delivers only 1.0 V when supplying 1 A.

## Battery capacity
Battery capacity is measured in ampere-hours (Ah) or milliampere-hours (mAh): the current a battery can supply
multiplied by the hours it lasts. A 3,000 mAh phone battery could in theory supply 300 mA for 10 hours. Multiplying by
the voltage gives the stored energy, so 3 Ah at 3.7 V is about 11 watt-hours.

## Capacitors
A capacitor stores electric charge on two conducting plates separated by an insulator. Its capacitance, C = Q / V, is
measured in farads; a 100 µF capacitor charged to 10 V holds 1 mC of charge. Capacitors release their energy quickly,
smooth out voltage ripples in power supplies and set the timing in circuits.

## Voltage dividers
A voltage divider is two resistors in series that split an input voltage into a smaller output. The output across the
second resistor is V_out = V_in × R₂ / (R₁ + R₂). With 12 V across a 10 kΩ and a 2 kΩ resistor in series, the 2 kΩ
resistor has 2 V across it. Many sensor circuits are wired this way.
