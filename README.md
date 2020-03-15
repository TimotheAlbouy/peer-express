# v1. PeerExpress: distributed version

PeerExpress is a decentralized messaging application. Even though it exchanges the messages in a peer-to-peer manner,
it uses a centralized signaling server for negotiating the session between two users.

**PeerExpress is not able to traverse NAT**, i.e. you cannot use it on the Internet but only on private networks.

## Application architecture

The application is composed of 4 sub-project:

* `joram-server` and `joram-admin` allow respectively to launch and administrate a JORAM server. Every peer has a local
JORAM server for receiving messages, and uses the administration module to create a queue on it. The `joram-server`
module is different from the version given during class because we can give in parameter the path of the JORAM
configuration folder, allowing to launch multiple servers on the same computer.

* `peer-express-signaling` is a SOAP signaling server that leverages jax-ws. Every time they log in, the users register
their username/IP address/opened port on this server, and retrieve the list containing the credentials of all the other
users.

* `peer-express-client` harnesses a JORAM server to receive messages from other users. It creates a session with
another user the first time the local user sends a message. The application has 2 different user interfaces: a
command-line interface and a graphic user interface.

### Project build

The application is distributed as a Maven project composed of 4 sub-projects containing a `src/` directory that
contains the java sources and `pom.xml` file containing the project description. There is also a `pom.xml` file at the
root of the project for the general Maven project description.

To build the project, go first to the root of the `peer-express-signaling` module and build it by typing:

    mvn install

Then, go back to the root directory and launch the server's jar:

    java -jar peer-express-signaling/target/peer-express-signaling-1.jar <port>

Once the WSDL is published, you can build the whole project by typing:

    mvn install

### Application execution

Launch the clients by typing:

    java -jar peer-express-client/target/peer-express-client-1.jar <username> <port> <config path> (--console)?
