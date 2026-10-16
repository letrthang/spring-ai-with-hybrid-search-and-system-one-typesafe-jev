# Physics basics — acoustics and ultrasound

## What acoustics studies
Acoustics is the branch of physics that studies how sound is produced, how it travels and how it is received. Its
practical branches include room and building acoustics, noise control, musical acoustics, underwater acoustics and
ultrasonics. Acoustic engineers design quiet offices, clear-sounding concert halls, hearing aids and the sonar
systems used by ships.

## Reverberation time
Reverberation time, RT60, is the time it takes for sound in a room to fade by 60 dB after the source stops.
Sabine's formula estimates it as RT60 = 0.161 V / A, where V is the room volume in m³ and A is the total
absorption in m². Classrooms aim for about 0.4 to 0.6 s so speech stays clear, while concert halls for
orchestras are often around 2 s.

## Sound absorption
Sound absorption is the conversion of sound energy into a tiny amount of heat when sound hits a soft or porous
material. Each surface has an absorption coefficient between 0 and 1: bare concrete is about 0.02, while thick
acoustic foam or mineral wool can exceed 0.8 at mid frequencies. Adding curtains, carpets and ceiling panels
shortens the reverberation time and makes a room less echoey.

## Sound insulation and the mass law
Sound insulation is the ability of a wall or floor to stop sound passing from one room to the next, measured as a
reduction in decibels. According to the mass law, doubling the mass per square metre of a single wall improves
insulation by up to about 6 dB. Small gaps around doors and pipes leak sound badly, so sealing them matters as much
as making walls heavy. Absorbing materials such as acoustic foam improve the sound inside a room but do little to
block sound between rooms.

## Acoustic impedance and reflection
Acoustic impedance is a material's resistance to sound passing through it, equal to its density times its speed of
sound, Z = ρ v. When sound meets a boundary between two materials with very different impedances, most of the sound
is reflected. Air has an impedance of about 415 rayl and soft tissue about 1.6 million rayl, so about 99.9 % of
ultrasound is reflected at an air gap between probe and skin, which is why scanners use a coupling gel.

## Piezoelectric transducers
A piezoelectric transducer creates and detects ultrasound using a crystal or ceramic, such as quartz or lead
zirconate titanate (PZT), that changes shape when a voltage is applied. Driving the crystal with an alternating
voltage makes it vibrate at ultrasonic frequencies. When a returning echo squeezes the same crystal, the crystal
produces a small voltage, so one transducer can both send and receive.

## How ultrasound imaging works
Ultrasound imaging builds a picture from the echoes of short ultrasound pulses sent into the body. The scanner
assumes sound travels at 1,540 m/s in soft tissue, so it calculates depth from the echo time with d = v t / 2; an
echo from 1 cm deep returns after about 13 µs. Repeating this along many scan lines produces a moving image at
around 20 to 60 frames per second, without any ionising radiation.

## Choosing an ultrasound frequency
Medical ultrasound usually uses frequencies between about 2 and 15 MHz, chosen as a trade-off between image detail
and depth. Higher frequencies have shorter wavelengths and give sharper images but are absorbed faster: at 5 MHz
the wavelength in tissue is about 0.3 mm. Abdominal and pregnancy scans use 2 to 5 MHz to reach 15 to 20 cm deep,
while scans of tendons or the thyroid use 7 to 15 MHz for the first few centimetres.

## Ultrasonic cleaning and cavitation
Ultrasonic cleaning removes dirt by cavitation: tiny bubbles form and violently collapse in a liquid driven by
ultrasound, usually at 20 to 40 kHz. The collapsing bubbles scrub surfaces, including cracks that a brush cannot
reach. Ultrasonic baths clean jewellery, lab glassware and surgical instruments, and laboratory sonicators use the
same effect to break open cells and shear DNA.

## Ultrasonic distance sensors
An ultrasonic distance sensor measures range by sending a short burst of ultrasound, often at about 40 kHz, and
timing the echo. The distance is d = v t / 2, so an echo after 5.8 ms means an object about 1 m away in air at 20
°C. Car parking sensors, water-level sensors in tanks and robot obstacle detectors work this way, and better
sensors correct for temperature because the speed of sound changes with it.
