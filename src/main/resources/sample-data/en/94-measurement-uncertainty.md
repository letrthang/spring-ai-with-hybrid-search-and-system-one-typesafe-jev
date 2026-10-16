# Physics basics — measurement uncertainty and errors

## Error and uncertainty
Measurement error is the difference between a measured value and the true value, while measurement uncertainty is
the range in which the true value probably lies. Because the true value is never known exactly, the error itself
cannot be known, but the uncertainty can be estimated. A result is therefore reported as a best value with an
uncertainty, such as 9.79 ± 0.03 m/s².

## Instrument resolution and accuracy specifications
The resolution of an instrument is the smallest change it can display, and resolution sets a lower limit on the
uncertainty of a single reading. A digital balance that reads to 0.01 g cannot show differences smaller than that,
so its reading is uncertain by at least about ±0.005 g. Manufacturers also give an accuracy specification, such as
±(0.5 % of reading + 2 digits) for a digital multimeter, which is usually larger than the resolution.

## Zero error and parallax
A zero error is a systematic error in which an instrument shows a non-zero reading when it should read zero, such
as a balance showing 0.03 g with nothing on it. A zero error is removed by zeroing or taring the instrument, or by
subtracting the offset from every reading. Parallax error comes from reading a scale at an angle, so pointers,
rulers and the bottom of a liquid meniscus should be read with the eye level with the mark.

## Mean and standard deviation
When a measurement is repeated, the mean is the best estimate of the value and the standard deviation describes the
spread of single readings. For five pendulum timings of 2.01, 2.03, 1.98, 2.00 and 2.03 s, the mean is 2.01 s and
the standard deviation is about 0.02 s. For normally distributed readings, about 68 % fall within one standard
deviation of the mean and about 95 % within two.

## Standard error of the mean
The standard error of the mean shows how precisely an average is known, and equals the standard deviation divided
by the square root of the number of readings, s / √n. Five readings with a standard deviation of 0.02 s give a
standard error of about 0.009 s. Because of the square root, taking four times as many readings only halves the
uncertainty, and averaging never removes systematic errors.

## Adding and subtracting measurements
When measurements are added or subtracted, their absolute uncertainties combine. A simple worst-case rule adds the
uncertainties, so 25.0 ± 0.1 cm minus 10.0 ± 0.1 cm gives 15.0 ± 0.2 cm. When the errors are independent and
random, a better estimate adds them in quadrature, √(0.1² + 0.1²) ≈ 0.14 cm, because the two errors are unlikely to
be at their worst at the same time.

## Multiplying and dividing measurements
When measurements are multiplied or divided, their relative (percentage) uncertainties combine. For a density from
a mass of 50.0 ± 0.1 g (0.2 %) and a volume of 20.0 ± 0.4 cm³ (2 %), the result 2.50 g/cm³ has about 2.2 %
uncertainty, or ±0.06 g/cm³. For a power, the percentage uncertainty is multiplied by the power, so a 1 %
uncertainty in a radius gives a 2 % uncertainty in the area πr².

## Standard and expanded uncertainty
Standard uncertainty, written u, is an uncertainty expressed as one standard deviation, which covers the true value
with about 68 % confidence. Expanded uncertainty multiplies the standard uncertainty by a coverage factor, U = k u,
and k = 2 gives about 95 % confidence. Calibration certificates usually state expanded uncertainty with k = 2, so
±0.2 °C at k = 2 means a standard uncertainty of 0.1 °C.

## Rounding and reporting a result
A measured result is reported with its uncertainty rounded to one or two significant figures and the value rounded
to the same decimal place. A calculated g = 9.8127 m/s² with an uncertainty of 0.0347 m/s² should be written as
9.81 ± 0.03 m/s². The unit, the confidence level or coverage factor, and how the uncertainty was estimated should
be stated alongside the number.

## Error bars and comparing results
Error bars on a graph show the uncertainty of each plotted point as a line extending above and below the point. Two
results agree within their uncertainties when their ranges overlap, so g = 9.7 ± 0.2 m/s² agrees with the accepted
9.81 m/s², while 9.4 ± 0.1 m/s² does not and suggests a systematic error. A best-fit line that passes through most
of the error bars shows that the data are consistent with the model.
