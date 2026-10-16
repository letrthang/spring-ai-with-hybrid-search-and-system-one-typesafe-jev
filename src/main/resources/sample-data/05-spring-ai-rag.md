# How this demo works — RAG in short

## What is RAG
Retrieval-Augmented Generation (RAG) means: before a language model answers, the application looks up passages that
are related to the question and adds them to the prompt. The model then answers from those passages instead of
from memory only, so the answer can cite its sources and is less likely to be made up.

## Hybrid search
This demo searches Weaviate in two ways. BM25 ranks passages that share rare words with the question, such as the
code AER-01. Vector search ranks passages with a similar meaning, even when they use other words. Reciprocal Rank
Fusion merges both rankings: each passage gets 1 / (60 + rank) from every list it is in.

## System 1 and System 2
System 1 is a small, fast decision model (Laya). For each found passage it answers typed questions with a
probability: is the passage relevant, can it answer the question, does it try to give instructions. System 2 is a
large language model on OpenRouter that writes the final answer from the passages System 1 kept.
