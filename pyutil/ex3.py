weights = [
	[0.05, 0.15, 0.15, 0.25, 0.40],
	[0.00, 0.15, 0.10, 0.40, 0.35],
	[0.02, 0.06, 0.02, 0.15, 0.75],
	[0.70, 0.05, 0.05, 0.10, 0.10],
	[0.02, 0.06, 0.02, 0.45, 0.45] ]

for weight in weights:
	surfix = [".url",".title",".keywords",".inlink",".body"]
	with open("queries.txt") as f:
		content = f.readlines()
		for i in content:
			query = i.split(":",2)
			term =  query[1].rstrip().split(" ")
			term = ["#WSUM(" + ' '.join([str(weight[i]) + " " + t + surfix[i] for i in range(5)]) + ")" for t in term]
			print(query[0] + ":#AND(" + ' '.join(term) + ")")
		print("----------\n\n\n")
