/**
 * Copyright 2013 MIT Libraries
 * Licensed under: http://www.apache.org/licenses/LICENSE-2.0
 */

/**
 * Package contains a lightweight java library to support creation and consumption of BagIt-packaged content, as specified
 * by the BagIt IETF Draft Spec version 0.97. It represents an 'unconditionally compliant' implementation. It requires a 
 * Java 7 or better JRE to run, has a single dependency on the Apache commons compression library
 * for support of tarred Gzip archive format (".tgz"), and is Apache 2 licensed. The library attempts to simplify a few
 * of the most common use cases/patterns involving bag packages. The first (the 'producer' pattern) is where content
 * is assembled and placed into a bag, and the bag is then serialized for transport/hand-off to another component or system.
 * The library ensures that the constructed bag is correct. The second (the 'consumer' pattern) is where a bag serialization
 * (or a loose directory) is given and must be interpreted and validated for use.
 *
 * @author richardrodgers
 */
package edu.mit.lib.bagit;