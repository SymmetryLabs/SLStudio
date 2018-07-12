from flask import Flask, request

token_info = dict()

def block_until_token(sp_oauth):
	app = Flask(__name__)


	def shutdown_server():
	    func = request.environ.get('werkzeug.server.shutdown')
	    if func is None:
	        raise RuntimeError('Not running with the Werkzeug Server')
	    func()

	@app.route("/redirect")
	def redirect():
		code = request.args.get("code")
		global token_info
		token_info["access_token"] = sp_oauth.get_access_token(code)["access_token"]
		shutdown_server()
		return "You are now logged in"

	app.run(port=5555)

	return token_info["access_token"]

def main():
	print("STARTING")
	token = block_until_token(None)
	print("GOT", token)

if __name__ == '__main__':
	main()

