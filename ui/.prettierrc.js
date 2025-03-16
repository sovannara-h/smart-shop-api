module.exports = {
  // Longueur maximale de ligne avant retour à la ligne
  printWidth: 100,

  // Utiliser des guillemets simples au lieu des doubles
  singleQuote: true,

  // Pas de point-virgule à la fin des instructions
  semi: false,

  // Largeur de l'indentation (4 espaces est le standard)
  tabWidth: 4,

  // Utiliser des espaces au lieu des tabulations
  useTabs: false,

  // Ajouter des virgules finales là où c'est valide en ES5
  trailingComma: "es5",

  // Espaces autour des accolades dans les objets
  bracketSpacing: true,

  // Parenthèses autour du seul paramètre d'une fonction fléchée
  arrowParens: "always",

  // Retour à la ligne pour les attributs JSX
  jsxSingleQuote: false,

  // Position du > dans les balises JSX multilignes
  bracketSameLine: false,

  // Plugins
  plugins: [
    "prettier-plugin-tailwindcss", // Pour trier automatiquement les classes Tailwind
  ],

  // Fin de ligne cohérente entre OS
  endOfLine: "lf",

  // Ordre des imports
  importOrder: ["^react", "^@/(.*)$", "^[./]"],

  // Largeur maximale des commentaires
  proseWrap: "preserve",
};
