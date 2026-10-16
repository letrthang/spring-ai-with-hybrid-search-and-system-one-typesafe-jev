# Biotech basics — DNA sequencing

## What DNA sequencing is
DNA sequencing is reading the exact order of the bases A, T, G and C in a piece of DNA. It is used to identify
organisms, check cloned genes, find mutations that cause disease and study whole genomes. The first human genome took
about 13 years to finish, while today a genome can be read in about a day.

## Sanger sequencing
Sanger sequencing reads one DNA fragment at a time using chain-terminating bases. A polymerase copies the template,
and special dideoxy bases (ddNTPs), each tagged with a different coloured dye, stop the new strand at random points.
The fragments are sorted by size, and the colour at each length gives the sequence.

## Sanger read length and uses
A Sanger reaction gives one high-quality read of about 700–1,000 bases. It is very accurate, above 99.9% per base,
but slow and costly for large projects. It is still the standard for checking a single cloned gene, a PCR product or
a known mutation.

## Next-generation sequencing
Next-generation sequencing (NGS) reads millions to billions of DNA fragments at the same time. The DNA is broken into
short pieces, adapters are added, and each piece is copied and read in parallel. This massive parallel approach cut
the cost of a human genome from billions of dollars to a few hundred.

## Short-read sequencing
Short-read sequencing produces reads of about 50–300 bases each. A common method is sequencing by synthesis, where
fluorescent bases are added one at a time and a camera records the colour of each cluster after every step. Short
reads are cheap and accurate but struggle with long repeats.

## Long-read sequencing
Long-read sequencing reads single DNA molecules that are tens of thousands of bases long, sometimes over a million.
One method pulls DNA through a tiny protein pore and reads changes in electric current; another watches a single
polymerase working in real time. Long reads help assemble genomes and resolve repeats.

## Sequencing depth
Sequencing depth, or coverage, is the average number of reads that cover each base of the genome. A human genome is
often sequenced at about 30x, meaning each base is read about 30 times. Higher depth makes it easier to tell a real
mutation from a random sequencing error.

## Quality scores
Each base in a sequencing read comes with a quality score, called a Phred score, that estimates the chance it is
wrong. A score of Q20 means a 1 in 100 chance of error, and Q30 means 1 in 1,000. Low-quality bases, often at the ends
of reads, are trimmed before analysis.

## Library preparation
Library preparation turns a DNA sample into a form the sequencer can read. The DNA is cut into fragments of a chosen
size, often 200–500 base pairs, and short adapter sequences are joined to both ends. Adapters can carry barcodes so
many samples can be pooled and sequenced in one run.

## RNA sequencing
RNA sequencing (RNA-seq) measures which genes are switched on in a sample and how strongly. The RNA is first copied
into complementary DNA (cDNA) by reverse transcriptase, then sequenced like DNA. The number of reads that map to each
gene shows its expression level.
