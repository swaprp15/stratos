/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at

 *  http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.stratos.cli.commands;

import org.apache.commons.cli.*;
import org.apache.stratos.cli.Command;
import org.apache.stratos.cli.RestCommandLineService;
import org.apache.stratos.cli.StratosCommandContext;
import org.apache.stratos.cli.exception.CommandException;
import org.apache.stratos.cli.utils.CliConstants;
import org.apache.stratos.cli.utils.CliUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Deploy kubernetes host command.
 */
public class AddKubernetesHostCommand implements Command<StratosCommandContext> {

    private static final Logger logger = LoggerFactory.getLogger(AddKubernetesHostCommand.class);

    private Options options;

    public AddKubernetesHostCommand() {
        options = new Options();
        Option resourcePathOption = new Option(CliConstants.RESOURCE_PATH, CliConstants.RESOURCE_PATH_LONG_OPTION, true,
                "Kubernetes host resource path");
        resourcePathOption.setArgName("resource path");
        Option clusterIdOption = new Option(CliConstants.CLUSTER_ID_OPTION, CliConstants.CLUSTER_ID_LONG_OPTION, true,
                "Kubernetes cluster id");
        clusterIdOption.setArgName("cluster id");
        options.addOption(clusterIdOption);
        options.addOption(resourcePathOption);
    }

    @Override
    public String getName() {
        return "add-kubernetes-host";
    }

    @Override
    public String getDescription() {
        return "Add kubernetes host";
    }

    @Override
    public String getArgumentSyntax() {
    	return null;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public int execute(StratosCommandContext context, String[] args) throws CommandException {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing command: ", getName());
        }
        
        if ((args == null) || (args.length <= 0)) {
            context.getStratosApplication().printUsage(getName());
            return CliConstants.COMMAND_FAILED;
        }
       			
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine commandLine = parser.parse(options, args);
            
            if((commandLine.hasOption(CliConstants.RESOURCE_PATH)) && (commandLine.hasOption(CliConstants.CLUSTER_ID_OPTION))) {
	               
                // get cluster id arg value
            	String clusterId = commandLine.getOptionValue(CliConstants.CLUSTER_ID_OPTION);
                if (clusterId == null) {
                    context.getStratosApplication().printUsage(getName());
                    return CliConstants.COMMAND_FAILED;
                }
                
                // get resource path arg value
            	String resourcePath = commandLine.getOptionValue(CliConstants.RESOURCE_PATH);
                if (resourcePath == null) {
                    context.getStratosApplication().printUsage(getName());
                    return CliConstants.COMMAND_FAILED;
                }
                String resourceFileContent = CliUtils.readResource(resourcePath);
                
                RestCommandLineService.getInstance().addKubernetesHost(resourceFileContent, clusterId);
                return CliConstants.COMMAND_SUCCESSFULL;
            } else {
                context.getStratosApplication().printUsage(getName());
                return CliConstants.COMMAND_FAILED;
            }
           
        } catch (ParseException e) {
            logger.error("Error parsing arguments", e);
            System.out.println(e.getMessage());
            return CliConstants.COMMAND_FAILED;
        } catch (IOException e) {
            System.out.println("Invalid resource path");
            return CliConstants.COMMAND_FAILED;
        } catch (Exception e) {
            String message = "Unknown error occurred: " + e.getMessage();
            System.out.println(message);
            logger.error(message, e);
            return CliConstants.COMMAND_FAILED;
        }
    }
}