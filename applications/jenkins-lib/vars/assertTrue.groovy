/*
 * assertTrue or fail the build
 *
 * @param booleanValue is the expression to test
 * @param errorMessage is the error to be returned
*/
def call(booleanValue, errorMessage='Assertion failed') {
  if (!booleanValue) {
    error(errorMessage)
  }
}
