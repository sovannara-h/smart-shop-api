import { UserAuthForm } from "@/components/user-auth-form";
import { Link } from "react-router-dom";

export type  SignInProps = {

}

export const SignIn = (props: SignInProps) => {

    const {} = props;

    return (
      <div className={`SignIn`}>
        <div className="lg:p-8">
          <div className="mx-auto flex w-full flex-col justify-center space-y-6 sm:w-[350px]">
            <div className="flex flex-col space-y-2 text-center">
              <h1 className="text-2xl font-semibold tracking-tight">Connexion à votre compte</h1>
              <p className="text-muted-foreground text-sm">Entrez votre e-mail et mot de passe pour vous connecter</p>
            </div>
            <UserAuthForm />
            <p className="text-muted-foreground px-8 text-sm text-center">
              En continuant, vous acceptez nos{" "}
              <Link to="/terms" className="underline-offset-4 hover:text-primary underline">
                Conditions d'utilisation
              </Link>{" "}
              et notre{" "}
              <Link to="/privacy" className="underline-offset-4 hover:text-primary underline">
                Politique de confidentialité
              </Link>
              .
            </p>
          </div>
        </div>
      </div>
    );
}