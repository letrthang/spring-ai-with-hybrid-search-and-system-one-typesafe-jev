# Biotech basics — mass spectrometry of proteins

## What mass spectrometry measures
Mass spectrometry measures the mass of molecules by turning them into ions and sorting them by their mass-to-charge
ratio, written m/z. Every mass spectrometer has three parts: an ion source that gives molecules a charge, a mass
analyser that separates the ions, and a detector that counts them. The result is a mass spectrum, a plot of signal
intensity against m/z.

## The dalton
The dalton (Da) is the unit used for the mass of molecules in mass spectrometry. One dalton is one twelfth of the
mass of a carbon-12 atom, about the mass of one proton or one hydrogen atom. An average amino acid in a protein adds
about 110 Da, so a protein of 300 amino acids weighs roughly 33 kDa.

## Electrospray ionisation
Electrospray ionisation (ESI) turns proteins and peptides in solution into gas-phase ions without breaking them. The
liquid is sprayed from a fine needle held at a few kilovolts, forming charged droplets that shrink as the solvent
evaporates until ions are released. ESI usually puts many charges on a large protein, which gives a series of peaks,
called a charge-state envelope, that software converts back into a single mass.

## MALDI
MALDI, matrix-assisted laser desorption/ionisation, ionises molecules by firing a laser at a sample mixed with a
light-absorbing matrix. The sample and matrix dry together as crystals on a metal plate, and each laser pulse lifts
and ionises a small amount of material, mostly with a single charge. MALDI is fast and is used in clinical
microbiology labs to identify bacteria from their protein fingerprint in minutes.

## Mass analysers
A mass analyser separates ions according to their m/z. A time-of-flight (TOF) analyser measures how long ions take to
fly down a tube, since lighter ions arrive first, while a quadrupole uses oscillating electric fields to let only
chosen m/z values pass. Orbitrap and FT-ICR analysers measure how fast ions circle and reach very high resolution,
separating masses that differ by only a few parts per million.

## Peptide mass fingerprinting
Peptide mass fingerprinting identifies a protein from the masses of its peptides. The protein is first cut with the
enzyme trypsin, which cleaves after the amino acids lysine and arginine, giving a set of peptides with predictable
masses. The measured masses are compared with the peptides predicted from a protein database, and the best-matching
protein is reported.

## Tandem mass spectrometry
Tandem mass spectrometry, written MS/MS, selects one peptide ion, breaks it into fragments and measures the
fragments. Collisions with an inert gas usually break the peptide bonds, giving two ladders of fragments called b
ions and y ions. The mass differences between neighbouring fragments correspond to single amino acids, so the
sequence of the peptide can be read from the spectrum.

## Bottom-up proteomics
Bottom-up proteomics identifies thousands of proteins in a sample by first digesting them all into peptides. The
peptide mixture is separated by reversed-phase liquid chromatography and fed straight into the mass spectrometer, a
setup called LC-MS/MS. Database search software matches the fragment spectra to known sequences, and a single run of
1 to 2 hours can identify several thousand human proteins.

## Quantifying proteins by mass spectrometry
Mass spectrometry can measure how much of each protein is present, not just which proteins are there. Label-free
methods compare the signal of the same peptides between samples, while labelling methods such as SILAC and TMT mark
samples with stable isotopes so that several samples can be mixed and measured in one run. Targeted methods follow a
chosen list of peptides, often using known amounts of heavy-isotope-labelled peptides as internal standards.

## Intact mass analysis
Intact mass analysis measures the whole protein without cutting it into peptides. Intact mass analysis is used to
confirm that a recombinant protein has the expected sequence and to detect changes such as a missing amino acid or
added sugar groups. Glycosylation, phosphorylation (+80 Da) and oxidation (+16 Da) each shift the mass by a known
amount, so the size of a shift points to the type of modification.
