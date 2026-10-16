# Biotech basics — bioinformatics

## What bioinformatics is
Bioinformatics is the use of computers and statistics to store, search and analyse biological data. It deals mostly
with DNA, RNA and protein sequences, but also with gene activity and protein structures. A single sequencing run can
produce hundreds of gigabytes of data, far too much to read by hand.

## The FASTA format
FASTA is a simple text format for storing DNA or protein sequences. Each record starts with a header line beginning
with ">" followed by a name or description, and the sequence itself follows on the next lines. DNA uses the letters A,
C, G and T, while proteins use one letter for each of the 20 amino acids.

## The FASTQ format
FASTQ is the text format that sequencing machines use to store reads together with their quality scores. Each read
takes exactly four lines: a header starting with "@", the sequence, a "+" line and a line of quality characters. Each
character encodes the Phred quality score of the base above it.

## Sequence alignment
Sequence alignment lines up two or more sequences to find where they match. Gaps are inserted to account for bases
that were added or lost, and mismatches show substitutions. A global alignment compares sequences over their whole
length, while a local alignment finds the best-matching region inside them.

## BLAST searches
BLAST is a widely used program that finds sequences in a database that are similar to a query sequence. It works by
finding short exact matches first and then extending them. Each hit gets an E-value, the number of matches that good
expected by chance, so an E-value near zero means a meaningful match.

## Sequence identity and similarity
Sequence identity is the percentage of positions in an alignment where the two sequences have exactly the same base
or amino acid. Protein similarity also counts positions where the amino acids differ but have similar chemical
properties. Two proteins with more than about 30% identity over their length usually share the same fold.

## Genome assembly
Genome assembly is putting millions of short sequencing reads back together into the full genome sequence. Software
finds reads that overlap and joins them into longer stretches called contigs. Repeated sequences make assembly hard,
because reads from different copies of a repeat look identical.

## Read mapping
Read mapping is placing each sequencing read at its matching position on a known reference genome. It is faster than
building a new genome from scratch and is the first step in finding mutations. Mapped reads are stored in SAM or the
compressed BAM format.

## Variant calling
Variant calling is finding positions where a sample's DNA differs from the reference genome. Software looks at all
the reads mapped to each position and decides whether a difference is real or a sequencing error. The results, such as
single-base changes called SNPs, are saved in a VCF file.

## Phylogenetic trees
A phylogenetic tree is a branching diagram that shows how related different species, genes or virus strains are.
It is built by comparing their sequences, assuming that more differences mean a longer time since a common ancestor.
Trees are used to trace the evolution of species and the spread of outbreaks.
