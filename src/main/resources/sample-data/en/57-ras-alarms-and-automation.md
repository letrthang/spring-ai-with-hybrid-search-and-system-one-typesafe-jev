# RAS basics — Alarms and automation

## Warning and critical alarm levels
RAS alarms usually have two levels: a warning when a value starts to drift, and a critical alarm when fish are in
immediate danger. For example, a salmon tank might warn at DO below 7 mg/L and go critical below 5 mg/L. Warnings
give staff time to act before a crisis develops.

## Alarm delays and hysteresis
An alarm delay makes a value stay outside its limit for a set time, often 30 seconds to a few minutes, before the alarm
fires. Hysteresis means the alarm clears only when the value returns well inside the limit. Together they stop short
spikes and sensor noise from causing floods of false alarms.

## Alarm notification and escalation
When a RAS alarm fires, the monitoring system notifies the on-duty staff by phone call, text message or app. If nobody
acknowledges a critical alarm within a set time, such as 5 to 10 minutes, it escalates to the next person on the call
list. Night-time cover must always be arranged.

## Automatic backup oxygen
Automatic backup oxygen opens a valve to emergency diffusers in every tank when DO falls below a critical level or the
main oxygen supply fails. The valve is often normally open, so it opens by itself when power is lost. The backup is
usually fed from a liquid oxygen tank or high-pressure cylinders.

## Standby pumps
A standby pump is a second pump that starts automatically when the duty pump fails or flow drops below a set value.
Critical pumps, such as the main recirculation pumps, should always have a standby. Farms swap the duty and standby
pumps every week or month so both stay in working order.

## Power failure response
A power failure is the most dangerous event in a RAS, because pumps, oxygen supply and aeration all stop at once. A
backup generator should start automatically within seconds and take over the critical load. Battery-powered alarms
and backup oxygen must work even if the generator fails to start.

## Testing alarms and backup systems
Alarms and backup systems should be tested on a schedule, often weekly or monthly, by simulating a fault. Tests
include running the generator under load, opening backup oxygen valves and triggering alarms from each sensor. A
backup that has never been tested cannot be trusted in a real emergency.

## Sensor offline alarms
A sensor offline alarm fires when a sensor stops sending readings or sends impossible values, such as a frozen number
or a DO of zero. Without this alarm, a broken sensor could hide a real problem. Offline alarms should be treated as
seriously as a low reading until the cause is found.

## Interlocks between equipment
An interlock is an automatic rule that stops one piece of equipment when another fails, to protect fish or machinery.
For example, the ozone generator should stop when flow through the contact tank stops, and feeders should stop when
DO is critically low. Interlocks prevent one fault from causing a second.

## Avoiding alarm fatigue
Alarm fatigue happens when staff receive so many unimportant alarms that they start to ignore real ones. Farms avoid
it by setting sensible limits, using delays, fixing faulty sensors quickly and reviewing alarm logs every week. Each
alarm should require a clear action.
