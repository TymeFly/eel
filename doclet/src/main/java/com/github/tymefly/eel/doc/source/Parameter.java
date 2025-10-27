package com.github.tymefly.eel.doc.source;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.tymefly.eel.doc.utils.EelType;


/**
 * Description of a parameter
 * @param name               Name of the parameter as given in the Java code
 * @param type               The EEL type for the parameter, or {@literal null} if the parameter isn't
 *                           an EEL type.
 * @param index              0-based index of the parameter in the <b>Java</b> function
 * @param defaultDescription The optional description given in the {@link com.github.tymefly.eel.udf.DefaultArgument}
 *                           annotation
 * @param isVarArgs          {@literal true} only if this is a VarAgs parameter
 */
public record Parameter(@Nonnull String name,
                        @Nullable EelType type,
                        int index,
                        @Nullable String defaultDescription,
                        boolean isVarArgs) {
}
