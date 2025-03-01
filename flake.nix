{
    description = "A nix flake for minijava compiler";

    inputs = {
        nixpkgs.url = "github:nixos/nixpkgs/nixos-23.05";
    };

    outputs = { self, nixpkgs, ... }: let
        system = "x86_64-linux";
    in {
        devShells."${system}".default = let
            pkgs = import nixpkgs {
                inherit system;
                config = {
                    permittedInsecurePackages = [
                        "openssl-1.1.1v"
                    ];
                };
                overlays = [
                    (final: prev: {
                     gradle = prev.gradle.overrideAttrs (old: {
                             java = prev.jdk11;
                             version = "8.3";
                             src = builtins.fetchurl {
                             url = "https://services.gradle.org/distributions/gradle-8.3-bin.zip";
                             sha256 = "sha256:09cjyss4bcnig1wzhxpwyn4kznkawzaha7fy0jg5nqzw2ysma62r";
                             };
                             });
                     })
                ];
                jdk = pkgs.jdk11;
                permittedInsecurePackages = [
                    "openssl-1.1.1v"
                ];

            };
        in pkgs.mkShell {
            buildInputs = with pkgs; [
                jdk11
                gradle

            ];
            shellHook = ''
                '';
        };
    };
}
