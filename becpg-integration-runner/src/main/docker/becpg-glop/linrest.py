from flask import Flask, request
from ortools.linear_solver import pywraplp
from math import inf

class ProgramSpecificationError(Exception):
	def __init__(self, *args):
		if args:
			self.message = args[0]
		else:
			self.message = "Unknown error"

	def __str__(self):
		return "Linear program ill-specified: {}".format(self.message)

app = Flask(__name__)

def make_variables(data_vars, solver):
	var_dict = {}
	for var_name in data_vars:
		if var_name in var_dict:
			raise ProgramSpecificationError("Multiple variables named {}".format(var_name))

		var_dict[var_name] = solver.NumVar(0, solver.infinity(), var_name)
	return var_dict

def solve(content):
	if content == None:
		raise ProgramSpecificationError("Request was empty")
	name = "LinearProgram"
	if "name" in content:
		name = content["name"]
	solver = pywraplp.Solver(name, pywraplp.Solver.GLOP_LINEAR_PROGRAMMING)

	if "variables" not in content:
		raise ProgramSpecificationError("No variable list")

	var_dict = make_variables(content["variables"], solver)

	if "constraints" not in content:
		raise ProgramSpecificationError("No constraint list")

	for data_constr in content["constraints"]:
		if "lower" not in data_constr:
			raise ProgramSpecificationError("Constraint has no lower value")

		lower = 0
		if data_constr["lower"] == "-inf":
			lower = -inf
		elif data_constr["lower"] == "inf":
			raise ProgramSpecificationError("Constraint has lower bound = +inf")
		else:
			lower = data_constr["lower"]

		if "upper" not in data_constr:
			raise ProgramSpecificationError("Constraint has no upper value")

		upper = 0
		if data_constr["upper"] == "-inf":
			raise ProgramSpecificationError("Constraint has upper bound = -inf")
		elif data_constr["upper"] == "inf":
			upper = inf
		else:
			upper = data_constr["upper"]

		data_coeffs = {}
		if "coefficients" in data_constr:
			data_coeffs = data_constr["coefficients"]
		if len(data_coeffs) == 0:
			raise ProgramSpecificationError("Constraint has no coefficient data")

		constr = solver.Constraint(lower, upper)
		for var_name in data_coeffs:
			if var_name not in var_dict:
				raise ProgramSpecificationError(f"{var_name} is not a variable")
			constr.SetCoefficient(var_dict[var_name], data_coeffs[var_name])

	if "objective" not in content:
		raise ProgramSpecificationError("No objective specification")

	objective = solver.Objective()

	if "task" not in content["objective"]:
		raise ProgramSpecificationError("No objective function task")

	if content["objective"]["task"] == "min":
		objective.SetMinimization()
	elif content["objective"]["task"] == "max":
		objective.SetMaximization()

	data_coeffs = {}
	if "coefficients" in content["objective"]:
		data_coeffs = content["objective"]["coefficients"]
	if len(data_coeffs) == 0:
		raise ProgramSpecificationError("No objective function coefficients")

	for var_name in data_coeffs:
		if var_name not in var_dict:
			raise ProgramSpecificationError(f"{var_name} is not a variable")

		objective.SetCoefficient(var_dict[var_name], data_coeffs[var_name])

	status = solver.Solve()
	if status == solver.OPTIMAL or status == solver.FEASIBLE:
		return {
			"value": objective.Value(),
			"coefficients": {key: var_dict[key].solution_value() for key in var_dict},
			"status": "optimal" if status == solver.OPTIMAL else "feasible"
		}
	else:
		return None

@app.route("/compute", methods = ["POST"])
def compute():
	try:
		print(request.get_json())
		response = solve(request.get_json())
		print(response)
		if response != None:
			return response, 200
		else:
			return "", 200
	except ProgramSpecificationError as e:
		print(e)
		return f"{e}", 400
	except Exception as e:
		print(e)
		return f"{e}", 500

if __name__ == "__main__":
	app.run(host = "0.0.0.0", debug = True)
