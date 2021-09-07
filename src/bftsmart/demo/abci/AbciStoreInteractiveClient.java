/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package bftsmart.demo.abci;

import bftsmart.demo.map.MapClient;
import bftsmart.tom.ServiceProxy;
import io.netty.channel.sctp.SctpNotificationHandler;

import java.io.*;
import java.util.Scanner;
import java.util.Set;

/**
 * Interactive runnable class to use AbciStoreClient
 *
 * @author rSimoes
 */
public class AbciStoreInteractiveClient {

    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            System.out.println("Usage: demo.map.MapInteractiveClient <client id>");
        }

        int clientId = Integer.parseInt(args[0]);
        AbciStoreClient map = new AbciStoreClient(clientId);
        Scanner sc = new Scanner(System.in);

        boolean exit = false;
        String key, value, result;
        while(!exit) {
            System.out.println("Select an option:");
            System.out.println("0 - Terminate this client");
            System.out.println("1 - Insert value into the map");
            System.out.println("2 - Retrieve value from the map");

            System.out.print("Option: ");
            int cmd = Integer.parseInt(sc.nextLine());

            switch (cmd) {
                case 0:
                    map.close();
                    exit = true;
                    break;
                case 1:
                    System.out.println("Putting value in the map");
                    System.out.println("Enter the key:");
                    key = sc.nextLine();
                    System.out.println("Enter the value:");
                    value = sc.nextLine();
                    result =  map.put(key, value);
                    System.out.println("Previous value: " + result);
                    break;
                case 2:
                    System.out.println("Reading value from the map");
                    System.out.print("Enter the key:");
                    key = sc.nextLine();
                    result =  map.get(key);
                    System.out.println("Value read: " + result);
                    break;
                default:
                    break;
            }
        }
    }
}
