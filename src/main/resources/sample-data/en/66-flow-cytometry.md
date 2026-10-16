# Biotech basics — flow cytometry

## What flow cytometry is
Flow cytometry is a method that measures thousands of single cells per second as they pass one by one through a laser
beam. For each cell, the flow cytometer records how much light the cell scatters and how much fluorescence it gives
off. A typical run analyses 10,000 to 1,000,000 cells in a few minutes and reports statistics on cell size, internal
complexity and labelled markers.

## Hydrodynamic focusing
Hydrodynamic focusing is how a flow cytometer lines cells up in single file. The sample stream is injected into the
centre of a faster-moving sheath fluid, which squeezes the sample into a narrow core only a few micrometres wide.
Because the cells cross the laser spot one at a time, each burst of light belongs to a single cell.

## Forward and side scatter
Forward scatter (FSC) and side scatter (SSC) are the two light-scattering signals a flow cytometer measures without
any stain. FSC is light deflected at small angles along the laser path and roughly reflects cell size. SSC is light
bounced out at about 90 degrees and reflects internal complexity such as granules, so neutrophils show high SSC while
lymphocytes show low SSC.

## Fluorescent labels in flow cytometry
Fluorescent labels let flow cytometry measure specific molecules on or inside cells. Antibodies coupled to dyes such
as FITC or PE bind surface markers like CD4 or CD8, while DNA dyes such as propidium iodide measure DNA content or
mark dead cells. Each dye absorbs light from one laser and emits at a longer wavelength; FITC, for example, is
excited at 488 nm and emits near 520 nm.

## Lasers, filters and detectors
A flow cytometer separates colours with lasers, optical filters and light detectors. Common instruments carry two to
five lasers, for example at 405, 488 and 640 nm, and dichroic mirrors and band-pass filters send each colour band to
its own photomultiplier tube. Modern analysers can measure 20 or more fluorescent parameters on every cell at the
same time.

## Gating
Gating is the step in flow cytometry analysis where the user draws regions on plots to select a cell population. A
first gate on FSC versus SSC removes debris, a second gate removes doublets where two cells stuck together, and later
gates pick cells that are positive for chosen markers. Each result is reported as a percentage of the parent
population, for example 45 % CD4-positive T cells among lymphocytes.

## Compensation
Compensation is the correction for spectral overlap between fluorescent dyes in flow cytometry. Dye emission spectra
are broad, so some FITC light spills into the PE detector, and without correction cells could appear falsely
double-positive. Single-stained control samples are run to measure the spillover of each dye, and the software
subtracts that fraction from the other channels.

## Controls in flow cytometry
Flow cytometry experiments need controls to decide where positive staining begins. An unstained sample shows the
natural autofluorescence of the cells, and fluorescence-minus-one (FMO) controls, which contain every dye except one,
set the gate boundary for the missing colour. Viability dyes and isotype controls help separate true signal from dead
cells and non-specific antibody binding.

## Cell sorting (FACS)
Fluorescence-activated cell sorting, or FACS, is a form of flow cytometry that physically separates cells into tubes
based on their signals. The fluid stream is vibrated so that it breaks into tiny droplets holding at most one cell
each, and droplets carrying a wanted cell receive an electric charge and are deflected by charged plates into a
collection tube. Cell sorters can process tens of thousands of cells per second and reach purities above 95 %.

## Common uses of flow cytometry
Flow cytometry is widely used in hospitals and research labs. Clinical labs count CD4 T cells to monitor HIV
infection, diagnose leukaemias and lymphomas from their surface markers, and count CD34-positive stem cells before
transplants. In research, flow cytometry measures the cell cycle, detects apoptosis with annexin V staining, and
checks transfection efficiency with GFP reporters.
