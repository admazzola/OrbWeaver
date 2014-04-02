Weaver
======

Requires Libraries: Google's guava-16.0.1.jar, Apache's commons-io-2.4.jar

This single-file P2P network API was originally written to provide P2P patches to the indie game Sands of Osiris.

This API can be divided into two part: Weaver, and WeaverOrb (or just Orb)

The Weaver portion accepts two inputs: int Port; string[] MasterNodeIPAddresses. It outputs a Node[] list, which is a list of all other PCs running this copy of Weaver using the same port and Master Servers.

The WeaverOrb portion is an attachment, which also adds single-file sharing on top of the P2P connectivity.  It also accepts two inputs: String pathToFile; Weaver weaver.  It provides no outputs to the program it is implemented to.

The intended use is as follows:

Weaver and Orb are implemented in your application (like a game launcher/patcher maybe?) like this:

--BEGIN SAMPLE CODE--

    int PORT = 34567; //change this
    String MASTER_SERVER_IP_1 = "1.1.1.1"; //change this.. can have anywhere from 1 to infinite master servers
    String MASTER_SERVER_IP_2 = "1.1.1.2"; //change this.. optional to include at all
    String PATH_TO_FILE = "C:" + "//" + test.txt"; //change this
    

    Weaver weaver = new Weaver( PORT, new String[]{ MASTER_SERVER_IP_1, MASTER_SERVER_IP_2 } );
                
    WeaverOrb orb = new WeaverOrb( PATH_TO_FILE, weaver );
		orb.start();		 
		      
		weaver.registerOrb(orb);
		weaver.start();
		
--END SAMPLE CODE--

Once the library and this code has been implemented, master servers (master nodes) have to be set up.  They do not have to be extremely powerful, but they are required to get the P2P network up and running while it is in its infancy.

A master node is a computer, at one of the hardcoded IP address, that is running the application that has Weaver integrated, and that has the File that you want to share at the path that you defined.  You will probably need to forward the port or use UPNP in your application to permit network activity.


~General Explanation~
Once a master node is set up at the hardcoded IP address with the file at the filepath, other nodes (peers) can run the application and their weaver will detect if they do or do not have the same file at their filepath that the master server(s) tell them they should have there.  

If a peer has the file at the filepath (validated by a SHA-256 hash code from a Master Node) then that peer will be a Seeder who will help the Master Nodes distribute the file.

If a peer does not have the file at the filepath or it is the wrong file, that peer will be a Leecher.  Leechers ask Seeders for chunks (byte[]) of the file, then they compile all of those chunks to the file to become... a seeder!


This is intended to make a great P2P patching API for indie games that use a launcher that is coded in Java.  Minecraft is a perfect example of this.  Unfortunately, Minecraft uses a central server for patching.

Cheers!

http://www.starflask.com
