# Biotech basics — qPCR and RT-PCR

## What qPCR measures
Quantitative PCR (qPCR) measures how much of a specific DNA sequence is in a sample by following PCR amplification
as it happens. A fluorescent signal is read after every cycle, so the instrument records a curve of DNA amount
against cycle number. qPCR is also called real-time PCR and is used for gene expression studies, virus detection
and checking for genetically modified material in food.

## RT-PCR and RT-qPCR
RT-PCR, or reverse transcription PCR, is PCR that starts from RNA instead of DNA. First the enzyme reverse
transcriptase copies the RNA into complementary DNA (cDNA), and then ordinary PCR amplifies the cDNA. When the PCR
step is run as real-time PCR the method is called RT-qPCR, the standard test for RNA viruses such as SARS-CoV-2 and
for measuring mRNA levels; the "RT" means reverse transcription, not real time.

## Priming the reverse transcription
Reverse transcription in RT-PCR can be primed in three common ways. Oligo(dT) primers bind the poly(A) tail and
copy only mRNA, random hexamers of mixed six-base sequence bind all along every RNA, and gene-specific primers copy
only one target. One-step kits run reverse transcription and PCR in the same tube, while two-step methods make a
cDNA pool that can be tested for many genes.

## SYBR Green detection
SYBR Green is a fluorescent dye that binds to any double-stranded DNA and glows about 1,000 times brighter when
bound. As PCR product builds up, the SYBR Green signal rises with it. SYBR Green is cheap and works with any primer
pair, but because it also detects primer dimers and wrong products, a melt curve is needed to check that only one
product was made.

## Hydrolysis probes
A hydrolysis probe, often called a TaqMan probe, is a short labelled DNA that binds inside the target sequence
between the two primers. The probe carries a reporter dye at one end and a quencher at the other, so it stays dark
while intact; when Taq polymerase extends through it, the enzyme's 5′ to 3′ exonuclease activity cuts the probe and
frees the reporter to glow. Probes are more specific than SYBR Green and, with different dye colours, let several
targets be measured in one tube.

## The amplification curve and threshold
A qPCR amplification curve has three phases: a flat baseline, an exponential phase and a plateau. During the
exponential phase the product roughly doubles every cycle, and only this phase is used for quantification. The
threshold is a fluorescence level set above the baseline noise within the exponential phase, and the cycle at which
a curve crosses it is the Cq, also called the Ct value.

## Standard curves and efficiency
A qPCR standard curve is made by running a 10-fold dilution series of known template and plotting Ct against the
log of the starting amount. At 100% efficiency the slope is −3.32, meaning the Ct rises by about 3.3 cycles for
every tenfold dilution. Good assays usually have efficiencies of 90–110%, which is a slope of about −3.6 to −3.1,
and an R² above 0.98.

## Relative quantification with ΔΔCt
The ΔΔCt method compares how much a gene is expressed in a treated sample versus a control without needing a
standard curve. Each target Ct is first normalised to a reference gene such as GAPDH to give ΔCt, then the treated
ΔCt is compared with the control ΔCt to give ΔΔCt, and the fold change is 2^(−ΔΔCt). For example, a ΔΔCt of −2
means a fourfold increase; the ΔΔCt method assumes both genes amplify with close to 100% efficiency.

## Melt curves
A melt curve checks the product of a SYBR Green qPCR run. After the last cycle the instrument slowly heats the
samples from about 60 °C to 95 °C while reading fluorescence, which drops when the double-stranded DNA separates. A
single sharp peak in the melt curve means one specific product, while extra peaks at lower temperatures usually
point to primer dimers or non-specific amplification.

## Controls in qPCR
Controls show whether qPCR results can be trusted. A no-template control (NTC), containing water instead of sample,
should give no signal and detects contamination, while a no-RT control, an RNA sample processed without reverse
transcriptase, reveals contaminating genomic DNA. Running each sample in technical triplicate and including several
biological replicates helps separate real differences from pipetting noise.
