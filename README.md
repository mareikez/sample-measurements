# sample-measurements 
Samples measurements from a medical device to a grid. The grid is build in a 5 minute interval by default. 
There are different types of measurements e.g. temperature, spo2, heart rate. Every type is sampled separately. 
From each interval the last value of a measurement type is sampled to the grid. A value on the grid belongs to the 
grid represented by the measurement time. 

## Implementation
The project is only intended to show the logic of the sampling. There is no possibility to execute it as a program. 
The best way to check the sampling is to execute the tests. The logic is implemented in com.draeger.MeasurementSampler.
