/**
 * Functions that read data from local files.
 * <br>
 * To guard against potential denial-of-service attacks, a limit is imposed on the amount of data each function can
 * read. By default, this is {@value com.github.tymefly.eel.EelContext#DEFAULT_IO_LIMIT} bytes, but it can be changed
 * in the EEL context. If a function attempts to read beyond this limit, an {@link java.io.IOException} is thrown.
 */
@GroupDescription("io")
package com.github.tymefly.eel.function.io;

import com.github.tymefly.eel.udf.GroupDescription;