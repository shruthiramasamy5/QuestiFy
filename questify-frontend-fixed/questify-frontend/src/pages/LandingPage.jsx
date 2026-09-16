import Hero from "../components/landing/Hero.jsx";
import TrustStrip from "../components/landing/TrustStrip.jsx";
import Features from "../components/landing/Features.jsx";
import HowItWorks from "../components/landing/HowItWorks.jsx";
import Roles from "../components/landing/Roles.jsx";
import Showcase from "../components/landing/Showcase.jsx";
import Pricing from "../components/landing/Pricing.jsx";
import ClosingCta from "../components/landing/ClosingCta.jsx";

export default function LandingPage() {
  return (
    <>
      <Hero />
      <TrustStrip />
      <Features />
      <HowItWorks />
      <Roles />
      <Showcase />
      <Pricing />
      <ClosingCta />
    </>
  );
}
