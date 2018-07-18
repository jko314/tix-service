# tix-service

The service was implemented with non-blocking support in mind. Compare and swap feature within the aotmic packge was used. And the support for available seats was implemented using a non-blocking atomic counter for speed, since that service would be most heavily used.

The timeout support was done via a on-demand expiration of holds, where the check for expiration is performed anytime we want to try to perform a hold. 

Assumption was made that if we are not able to get all the tickets requested that we would return null.
Also, we assume that the lower the ticket number were better so the sequentially going through the array of ticket would return best possible tickets.

## build and run
mvn clean test
