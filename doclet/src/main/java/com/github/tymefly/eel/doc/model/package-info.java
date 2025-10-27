/**
 * The entry point to this package is the {@link com.github.tymefly.eel.doc.model.ModelManager}
 *
 * The models in this package are:
 * <ul>
 *  <li>Text - a single text fragment with an associated style</li>
 *  <li>Paragraph - an abstract ordered collection of Text models</li>
 *  <li>Passage - a concrete implementation of a Paragraph that is unbound to any other model</li>
 *  <li>Param - a single parameter passed to a function</li>
 *  <li>Tag - a block of text that has a specific meaning in the context of a function description </li>
 *  <li>Function - a description of a single function. This contains child Sections and an ordered list of Params</li>
 *  <li>Group - a collection of EEL function models, that have a common purpose</li>
 * </ul>
 *
 * Each model in this package is implemented as:
 * <ul>
 *     <li>xxxxx - concrete implementation of a model. These classes are all package-protected</li>
 *     <li>xxxxxModel - getter functions for the model which are used by the reports</li>
 *     <li>xxxxxGenerator - setter functions for the model which are used by the scanners. The methods in
 *              this interface are:
 *              <ul>
 *                  <li>with_____ - mutate the model. The returned object is the model for a fluent interface</li>
 *                  <li>add____ - create a new model element. The returned object is the new object </li>
 *              </ul>
 * </ul>
 *
 * The exception to this is are
 * <ul>
 *     <li>Text - which does not have a Generator interface as it is immutable</li>
 *     <li>Group - which does not have a Generator interface as it's only mutated by classes in this package </li>
 * </ul>
 */


package com.github.tymefly.eel.doc.model;