import java.text.Normalizer

/*
 * Normalize branch name by removing Norwegian characters and
 * running it through Normalizer with canonical decomposition.
 * CamelCase is converted to dash-case.
 *
 * @params branch name string, e.g. feature/kjøpeEnPæreis
 * @return feature-kjope-en-pareis
*/
def call(branch) {
  def aoao = branch.
    replaceAll(/[ÆÅ]/, 'A').
    replaceAll(/Ø/, 'O').
    replaceAll(/[æå]/, 'a').
    replaceAll(/ø/, 'o');

  Normalizer.normalize(aoao, Normalizer.Form.NFD).
    replaceAll(/\//, '-'). // Slash to dash
    replaceAll(/ +/, '-'). // Space to dash
    replaceAll(/[^A-z0-9\-]/, ''). // Remove what hasn't been covered
    replaceAll(/([A-Z])/, /-$1/ ).toLowerCase(). // Camel case to dash-case
    replaceAll(/--/, '-');
}
