/*
 * Copyright The OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opentelemetry.auto.instrumentation.lettuce;

import io.lettuce.core.protocol.RedisCommand;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LettuceInstrumentationUtil {

  public static final String[] NON_INSTRUMENTING_COMMAND_WORDS =
      new String[] {"SHUTDOWN", "DEBUG", "OOM", "SEGFAULT"};

  public static final Set<String> nonInstrumentingCommands =
      new HashSet<>(Arrays.asList(NON_INSTRUMENTING_COMMAND_WORDS));

  /**
   * Determines whether a redis command should finish its relevant span early (as soon as tags are
   * added and the command is executed) because these commands have no return values/call backs, so
   * we must close the span early in order to provide info for the users
   *
   * @param command
   * @return true if finish the span early (the command will not have a return value)
   */
  public static boolean doFinishSpanEarly(final RedisCommand command) {
    final String commandName = LettuceInstrumentationUtil.getCommandName(command);
    return nonInstrumentingCommands.contains(commandName);
  }

  /**
   * Retrieves the actual redis command name from a RedisCommand object
   *
   * @param command the lettuce RedisCommand object
   * @return the redis command as a string
   */
  public static String getCommandName(final RedisCommand command) {
    String commandName = "Redis Command";
    if (command != null) {
      /*
      // Disable command argument capturing for now to avoid leak of sensitive data
      // get the arguments passed into the redis command
      if (command.getArgs() != null) {
        // standardize to null instead of using empty string
        commandArgs = command.getArgs().toCommandString();
        if ("".equals(commandArgs)) {
          commandArgs = null;
        }
      }
      */

      // get the redis command name (i.e. GET, SET, HMSET, etc)
      if (command.getType() != null) {
        commandName = command.getType().name().trim();
        /*
        // if it is an AUTH command, then remove the extracted command arguments since it is the password
        if ("AUTH".equals(commandName)) {
          commandArgs = null;
        }
        */
      }
    }
    return commandName;
  }
}
