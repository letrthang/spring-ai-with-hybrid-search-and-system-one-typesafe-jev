# RAS basics — AI monitoring and anomaly detection

## What AI monitoring means in a RAS
AI monitoring in a RAS is software that reads the farm's sensor and operating data and uses machine learning to
spot unusual patterns and predict problems. AI monitoring works on top of the normal alarm system rather than
replacing it. The goal is to warn operators earlier and with more context than fixed alarm limits can give.

## The data an AI monitoring system uses
An AI monitoring system for a RAS uses logged sensor values such as dissolved oxygen, temperature, pH, ORP, flow
and water level, together with feeding times, feed amounts, biomass and maintenance records. The more consistent
and complete the history, the better the system learns. Most systems need several weeks to a few months of clean
data before their predictions are reliable.

## Learning normal patterns
An AI monitoring system learns what is normal for each tank, such as the daily dip in oxygen after every feeding
and the slow rise in CO2 as fish grow. Instead of one fixed limit, the system builds an expected range that changes
with time of day, feeding and biomass. A value is treated as unusual when it leaves that expected range, even if it
is still inside the fixed alarm limits.

## Anomaly detection
Anomaly detection is the part of an AI monitoring system that finds readings or combinations of readings that do
not fit the learned normal pattern. Examples are oxygen falling faster than usual after a meal, a flow sensor
reading that stays exactly constant for hours, or one tank behaving differently from identical tanks. Many
anomalies are early signs of equipment faults, sensor fouling or fish stress.

## Predicting oxygen problems
An AI monitoring system can predict a low-oxygen event by projecting the recent oxygen trend forward and comparing
it with what normally happens after feeding. If oxygen is likely to fall below the warning level within, for
example, 15–30 minutes, the system can alert operators before the alarm limit is reached. Early warning gives staff
time to check the oxygen supply, diffusers and pumps.

## Predicting ammonia and nitrite rises
An AI monitoring system can estimate the risk of rising TAN or nitrite by combining feed load, water temperature,
pH, biofilter flow and recent manual test results. A sharp increase in feeding, a temperature drop or a biofilter
flow problem can all raise the predicted risk. Because online ammonia sensors are less reliable, these predictions
should be confirmed with a photometer test.

## Explaining alerts
A useful AI alert explains why it was raised, for example by naming the sensors involved, showing the expected
range next to the actual values and pointing to recent events such as a feeding or a pump change. Clear
explanations help operators trust the alert and decide quickly what to check. An alert that cannot be explained is
hard to act on and is often ignored.

## Keeping a human in the loop
In a RAS, AI monitoring should advise people rather than take control of critical equipment on its own. Operators
confirm an alert, decide on the action and record what they found, while safety functions such as backup oxygen and
fixed alarms stay independent of the AI. Keeping a human in the loop prevents a wrong prediction from harming the
fish.

## Learning from operator feedback
Operator feedback improves an AI monitoring system over time, because each alert can be marked as a real problem, a
sensor fault or a false alarm. The system uses these labels to adjust its sensitivity and to learn new patterns,
such as the normal change after grading. Regular feedback reduces false alarms and helps avoid alarm fatigue.

## Limits of AI monitoring
AI monitoring is only as good as the data it learns from, so fouled or uncalibrated sensors lead to wrong
predictions. An AI monitoring system may also miss events it has never seen before, such as a new disease or a rare
equipment failure. Good sensor care, independent hard-wired alarms and experienced staff remain essential in every
RAS.
