# Apriori Algorithm in Map-Reduce.

## what is Association rule mining?

- Association rule mining is a rule-based machine learning method for discovering interesting relations between variables in large databases. It is intended to identify strong rules discovered in databases using some measures of interestingness. [Wikipedia]
- e.g. If item A is bought by customer c1 then chances of being bought item B by same customer on same transaction.
- A == > B, if A then B; A = antecedent and B = consequent.
- If we have huge dataset(transaction) then we can get so many combination of type
  A => B , A&B => C, A&B&C => D and so on so to find promising rules we need some metrices.
  Those metrices are follows:

1. Support :- support is frequency of item or items combination.

- supp(X)=Number of transaction in which X appears / Total number of transactions

2. Confidence: - How often items collection B occur given occurrence of items collection A.

- conf(X⟶Y)=supp(X∪Y) / supp(X)

3. Lift:- it’s Strength of any rule.

- lift(X⟶Y)=supp(X∪Y) / supp(X)∗supp(Y)

Where Map Reduce is Used?

- It is used for finding frequencies (to find value of support) of X in transactions.

<br>
References:

- https://dl.acm.org/doi/pdf/10.1145/2184751.2184842?casa_token=8kk1kzUT4TAAAAAA:S4rtH1FUiHCLjVMnK_a9li1m3Hjq-TabgVa43NsQ6ziX8sTCEzrwf3uDqax4KwgdFilE08qsALqSAEQ
- https://www.youtube.com/watch?v=guVvtZ7ZClw
