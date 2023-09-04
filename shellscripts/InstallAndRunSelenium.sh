# Function to check if command exist
is_command_exist()
{
        local arg="$1"
        type "$arg" &> /dev/null
        return $?

}

is_Folder_Exist()
{
        local arg="$1"
        cd /
        cd "$arg"
        ls -l &> /dev/null
        return $?

}
# Function to check if package installed
is_Package_Exist()
{
        local arg1="$1"
        dpkg -l | grep "$arg1" &> /dev/null

        return $?

}

# Function to install a specific package
install()
{
        local arg="$1"
        sudo apt-get update
        sudo DEBIAN_FRONTEND=noninteractive apt install -y "$arg"
}

# Function to unpack a debian
unpack_installer(){
    sudo dpkg -i "$1"
    sudo apt --fix-broken install
}



# check if Java exist or not:
#----------------------------
if is_command_exist "java"; then
        echo 'Java is already nstalled in this machine'
else
        echo 'Java is not installed in this Machine and it is installing now.....'
        install "openjdk-8-jdk"
fi
# check if Maven exist or not
#----------------------------
if is_command_exist "mvn"; then
        echo 'Maven is already nstalled in this machine'
else
        echo 'Maven is not installed in this Machine and it is installing now.....'
        install "maven"
fi
# check if Chrome exist or not:
#-----------------------------

if is_Package_Exist "chrome"; then
    echo "chrome is installed on this Ubuntu machine."
else
    echo "chrome is not installed."
    wget http://mirror.cs.uchicago.edu/google-chrome/pool/main/g/google-chrome-stable/google-chrome-stable_114.0.5735.198-1_amd64.deb
    unpack_installer "google-chrome-stable_114.0.5735.198-1_amd64.deb"
fi
# check if git exist or not
#--------------------------
#To be defined

# check if xvfb exist or not
#----------------------------
if is_Package_Exist "xvfb"; then
        echo 'xvfb is already installed in this machine'
else
    echo "Xvfb is not installed."
    install "xvfb"
fi

cd ~
