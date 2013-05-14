module Jenkins
  class SubversionScm < Scm

    def url(url)
      find(:path, path("locations/remote")).set(url)
    end
  end
end

Jenkins::Scm.register('Subversion', Jenkins::SubversionScm)
