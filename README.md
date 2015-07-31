common-libs-to-meteor connects the output of the common-libs DerivativeSetEngine class to 
a deployment of the SubscriptTables meteor project, so that users can see commodities risk data for 
contracts on the CME and ICE (mostly CME).  This project requires that you know a fair amount about 
meteor, commodities and risk.  However, an experienced java developer should be able to follow all of
the code with a little effort.  The meteor.js code in SubscriptTables requires a knowledge of both 
meteor and javascript.

SubscriptTables is a meteor.js project which you have to deploy either locally, on a local server or via 
the command: meteor deploy yourmeteorurl.meteor.com 


Classes that extend com.billybyte.commonlibstometeor.PositionBaseItem implement various 
methods to set up and send instances of themselves, and instances of MeteorTableModel to the 
meteor project SubscriptTables (running either on yourmeteorurl.meteor.com or running on a local server) so 
that the SubscriptTables meteor project can display those instances in the user's browser.

The main risk analytics performed are:

  1. Greeks
  2. P and L
  3. DeltaNormal VaR
  4. MonteCarlo VaR

As well, the GreeksInputData class implements methods to show all of the inputs that go
into the above 4 calculations, as well as building a MeteorTableModel to display that data.

TODO:

Much better documentation and getting started info.
There are .sh scripts to launch initializers and watchers which use meteor DDP to set up 
tables in SubscriptTables and to react to changes in the Position instances that are being 
displayed and entered within SubscriptTables (by users who have logged into their account at  
the deployment of SubscriptTables (e.g. yourmeteorurl.meteor.com).  
