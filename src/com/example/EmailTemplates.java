package com.example;

public class EmailTemplates {

    public static String approvalSubject() { return "Votre compte a été accepté"; }

    public static String approvalBody(User user) {
        String name = (user != null && user.getUsername() != null) ? user.getUsername() : "Utilisateur";
        return "Bonjour " + name + ",\n\n"
                + "Votre demande de création de compte a été ACCEPTÉE.\n"
                + "Vous pouvez maintenant vous connecter à l'application.\n\n"
                + "Cordialement,\nCloudDesk User Manager";
    }

    public static String rejectionSubject() { return "Votre demande a été refusée"; }

    public static String rejectionBody(User user) {
        String name = (user != null && user.getUsername() != null) ? user.getUsername() : "Utilisateur";
        return "Bonjour " + name + ",\n\n"
                + "Votre demande de création de compte a été REFUSÉE.\n"
                + "Si vous pensez qu'il s'agit d'une erreur, contactez le support.\n\n"
                + "Cordialement,\nCloudDesk User Manager";
    }

    public static String resetSubject() {
        return "Code de réinitialisation de mot de passe";
    }

    // ✅ FIX : token seul sur sa ligne (copier/coller fiable)
    public static String resetBody(String token) {
        return "Bonjour,\n\n"
                + "Voici votre code de réinitialisation de mot de passe (valide 60 minutes) :\n\n"
                + token + "\n\n"
                + "Instructions :\n"
                + "1) Retournez à l'application CloudDesk User Manager.\n"
                + "2) Cliquez sur le bouton \"J’ai un code\" sur l'écran de connexion.\n"
                + "3) Saisissez le code ci-dessus et votre nouveau mot de passe.\n\n"
                + "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n"
                + "Cordialement,\nCloudDesk User Manager";
    }
}
